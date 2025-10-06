import React, { createContext, useState, useContext, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import Keycloak from 'keycloak-js';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [role, setRole] = useState(null);
    const [userRoles, setUserRoles] = useState([]);
    const [keycloak, setKeycloak] = useState(null);
    const [loading, setLoading] = useState(true);
    const [userInfo, setUserInfo] = useState(null);
    const navigate = useNavigate();

    const isInitialized = useRef(false);
    const hasProcessedLogin = useRef(false);

    // Fetches full profile from backend and syncs it with Keycloak
    const fetchUserProfile = useCallback(async (token) => {
        try {
            const res = await fetch(`${process.env.REACT_APP_USER_SERVICE_URL}/user/profile`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (res.ok) {
                const profile = await res.json();
                console.log("User profile fetched:", profile);

                setUserInfo(prevInfo => ({
                    ...prevInfo,
                    ...profile
                }));
                return profile;
            } else {
                console.error("Failed to fetch user profile", res.status);
            }
        } catch (e) {
            console.error("error fetching user profile", e);
        }
    }, []);

    /**
     * Extracts user info, determines role, and redirects appropriately
     */
    const handleSuccessfulLogin = useCallback((kc) => {

        if (hasProcessedLogin.current) {
            console.log("Login already processed, skipping");
            return;
        }

        hasProcessedLogin.current = true;

        const decodedToken = kc.tokenParsed;

        const role = decodedToken?.role;
        setUserRoles(role);

        // Determine Role
        let primaryRole = "Unknown";
        if (role) {
            const normalizedRole = role.toLowerCase().replace(/\s+/g, '_');
            if (normalizedRole === 'doctor') {
                primaryRole = 'doctor';
            } else if (normalizedRole === 'other_staff') {
                primaryRole = 'other_staff';
            } else if (normalizedRole === 'patient') {
                primaryRole = 'patient';
            }
        }

        setRole(primaryRole);
        setIsLoggedIn(true);

        // Set user info
        const userInfoFromToken = {
            userId: decodedToken?.sub,
            username: decodedToken?.preferred_username,
            email: decodedToken?.email,
            firstName: decodedToken?.given_name,
            lastName: decodedToken?.family_name,
            role: primaryRole
        };
        setUserInfo(userInfoFromToken);
        localStorage.setItem('token', kc.token);

        // Fetch & sync user from backend
        fetchUserProfile(kc.token);

        // Redirect to profile if on home or login page
        const currentPath = window.location.pathname;
        if (currentPath === '/' || currentPath === '/login') {
            console.log("Redirecting to profile");
            // Use setTimeout to avoid immediate re-render conflicts
            setTimeout(() => {
                navigate('/profile', { replace: true });
            }, 100);
        }

    }, [fetchUserProfile, navigate]);
    /**
    * Initialize Keycloak on mount
    * Uses 'check-sso' mode to allow app to load without forcing login
    */
    useEffect(() => {

        if (isInitialized.current) {
            console.log("Keycloak already initialized, skipping");
            return;
        }

        isInitialized.current = true;
        console.log("Environment variables check:");
        console.log("Keycloak URL:", process.env.REACT_APP_KEYCLOAK_URL);
        console.log("Keycloak Realm:", process.env.REACT_APP_KEYCLOAK_REALM);
        console.log("Keycloak Client:", process.env.REACT_APP_KEYCLOAK_CLIENT_ID);

        // Check if environment variables are loaded
        if (!process.env.REACT_APP_KEYCLOAK_URL ||
            !process.env.REACT_APP_KEYCLOAK_REALM ||
            !process.env.REACT_APP_KEYCLOAK_CLIENT_ID) {
            console.error("Missing required environment variables for Keycloak configuration");
            setLoading(false);
            return;
        }

        const keycloakInstance = new Keycloak({
            url: process.env.REACT_APP_KEYCLOAK_URL,
            realm: process.env.REACT_APP_KEYCLOAK_REALM,
            clientId: process.env.REACT_APP_KEYCLOAK_CLIENT_ID,
        });

        keycloakInstance.init({
            onLoad: 'check-sso', // This allows the app to load without forcing login
            checkLoginIframe: false,
            pkceMethod: 'S256' // Add PKCE for better security
        })
            .then(authenticated => {
                console.log('Keycloak initialized. Authenticated:', authenticated);
                setKeycloak(keycloakInstance);

                if (authenticated) {
                    handleSuccessfulLogin(keycloakInstance);
                } else {
                    console.log("User not authenticated, but not forcing login (check-sso mode)");
                }

                setLoading(false);
            })
            .catch(err => {
                console.error('Keycloak initialization failed:', err);
                setLoading(false);
            });
    }, []);

    const login = () => {
        if (keycloak) {
            console.log("Initiating login...");
            hasProcessedLogin.current = false;
            keycloak.login();
        } else {
            console.error("Keycloak instance is null");
        }
    };

    const logout = () => {
        if (keycloak) {
            keycloak.logout({
                redirectUri: process.env.REACT_APP_BASE_URL || window.location.origin
            });
        }
        localStorage.removeItem('token');
        setIsLoggedIn(false);
        setRole(null);
        setUserRoles([]);
        setUserInfo(null);
    };

    // Token management
    const getToken = () => {
        return keycloak?.token || localStorage.getItem('token');
    };

    const refreshToken = () => {
        if (keycloak) {
            return keycloak.updateToken(30);
        }
        return Promise.reject('Keycloak not initialized');
    };

    const contextValue = {
        // Authentication state
        isLoggedIn,
        loading,

        // User information
        role,
        userInfo,

        // Authentication methods
        login,
        logout,

        // Token management
        getToken,
        refreshToken,

        // Additional methods
        refreshUserProfile: () => fetchUserProfile(getToken()),

        // Keycloak instance (for advanced usage)
        keycloak
    };

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
};
/**
 * Connect to AuthContext
 * @returns {Object} Authentication state and methods
 * @throws {Error} If used outside AuthProvider
 */
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};