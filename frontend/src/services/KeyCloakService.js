import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
    url: process.env.REACT_APP_KEYCLOAK_URL,
    realm: process.env.REACT_APP_KEYCLOAK_REALM,
    clientId: process.env.REACT_APP_KEYCLOAK_CLIENT_ID,
});

const keycloakService = {
    keycloak,
    init: (onAuthenticatedCallback) => {
        keycloak
            .init({
                onLoad: 'login-required',
                pkceMethod: 'S256',
                checkLoginIframe: false
            })
            .then((authenticated) => {
                if (authenticated) {
                    console.log("Authenticated");
                    console.log("User ID:", keycloak.subject);
                    console.log("Roles:", keycloak.realmAccess?.roles || []);
                    onAuthenticatedCallback(keycloak);
                } else {
                    console.log("Not authenticated");
                }
            })
            .catch((error) => {
                console.error("Keycloak initialization error:", error);
            });
    },
    login: () => {
        keycloak.login();
    },
    logout: () => {
        keycloak.logout({
            redirectUri: process.env.REACT_APP_BASE_URL
        });
    },
    getToken: () => keycloak.token || null,
    isAuthenticated: () => keycloak.authenticated || false,
};

export default keycloakService;
