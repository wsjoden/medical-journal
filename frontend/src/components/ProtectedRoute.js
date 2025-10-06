/**
 * Protected Route Component
 * 
 * Route wrapper that enforces authentication and role-based access control.
 * Prevents unauthorized users from accessing protected pages.
 *
 */

import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../services/AuthContext';

const ProtectedRoute = ({ allowedRoles }) => {
    const { role, isLoggedIn } = useAuth();

    if (!isLoggedIn) {
        return <Navigate to="/login" />;
    }

    if (allowedRoles && !allowedRoles.includes(role)) {
        return <Navigate to="/unauthorized" />;
    }

    return <Outlet />;
};

export default ProtectedRoute;
