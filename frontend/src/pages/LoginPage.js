import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../services/AuthContext';

const LoginPage = () => {
    const navigate = useNavigate();
    const { isLoggedIn, login, role, loading } = useAuth();

    useEffect(() => {
      

        if (isLoggedIn) {
            if (role === "Patient") {
                navigate('/profile');
            } else if (role === "Doctor") {
                navigate('/dashboard');
            } else if (role === "Other_Staff") {
                navigate('/messages');
            }
        }
    }, [isLoggedIn, role, navigate]);

    return (
        <div className="login-container">
            <h1>Redirecting to Keycloak login...</h1>
        </div>
    );
};

export default LoginPage;
