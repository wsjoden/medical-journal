import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
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

    const handleSuccessfulLogin = useCallback((kc) => {
        const decodedToken = kc.tokenParsed;
        console.log('Decoded token:', decodedToken);
        console.log('Full token info:', {
            userId: decodedToken?.sub,
            username: decodedToken?.preferred_username,
            email: decodedToken?.email,
            realmRoles: decodedToken?.realm_access?.roles || [],
            role: decodedToken?.role
        });

        const role = decodedToken?.role;
        setUserRoles(role);

        // Determine primary role for your medical system
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

        const currentPath = window.location.pathname;
        if (currentPath === '/' || currentPath === '/login') {
            navigate('/profile', { replace: true });
        }

        if (window.location.search.includes("code=") || window.location.hash.includes("code=")) {
            window.history.replaceState({}, document.title, "/profile");
        }

    }, [fetchUserProfile, navigate]);

    useEffect(() => {
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
    }, [handleSuccessfulLogin]);

    const login = () => {
        if (keycloak) {
            console.log("Initiating login...");
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

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};