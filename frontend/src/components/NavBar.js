/**
 * Navigation Bar Component
 * 
 * Responsive Bootstrap navbar that changes based on user authentication and role.
 * Displays different navigation options for patients, doctors, and other staff.
 * 
 * Roles and their navigation:
 * - Patient: Profile, Inbox, Images, My Records
 * - Doctor: Dashboard, Profile, Inbox, Images, Patients list
 * - Other Staff: Dashboard, Profile, Inbox, Images, Patients list
 */

import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container } from 'react-bootstrap';
import { useAuth } from "../services/AuthContext";
import '../styles/NavBar.css';


/**
 * Navigation for logged-out users
 * Shows only login/register option
 */
function LoggedOutNav() {
    const { login } = useAuth();

    const handleLoginClick = (e) => {
        e.preventDefault();
        login(); // Redirects to Keycloak
    };

    return (
        <Nav className="ms-auto">
            <span onClick={handleLoginClick} className="nav-link login-nav-link" style={{ cursor: 'pointer' }}>
                Login / Register
            </span>
        </Nav>
    );
}

/**
 * Navigation for Patients
 * Shows profile, inbox, images, and medical records
 */
function PatientNav({ onLogout, userInfo }) {
    return (
        <Nav className="ms-auto">
            <span className="nav-link user-info">
                User Info: {userInfo?.firstName || userInfo?.username || 'Patient'}
            </span>
            <Link to="/profile" className="nav-link">My Profile</Link>
            <Link to="/inbox" className="nav-link">Inbox</Link>
            <Link to="/image/list" className="nav-link">Images</Link>
            <Link to={`/patient/${userInfo.id}`} className="nav-link">My Records</Link>
            <span onClick={onLogout} className="nav-link logout-nav-link" style={{ cursor: 'pointer' }}>
                Logout
            </span>
        </Nav>
    );
}

/**
 * Navigation for Doctors
 * Shows profile, dashboard, inbox, images, and patient list
 */
function DoctorNav({ onLogout, userInfo }) {
    return (
        <Nav className="ms-auto">
            <span className="nav-link user-info">
                User Info: {userInfo?.firstName || userInfo?.username || 'Doctor'}
            </span>
            <Link to="/dashboard" className="nav-link">Dashboard</Link>
            <Link to="/profile" className="nav-link">My Profile</Link>
            <Link to="/inbox" className="nav-link">Inbox</Link>
            <Link to="/image/list" className="nav-link">Images</Link>
            <span onClick={onLogout} className="nav-link logout-nav-link" style={{ cursor: 'pointer' }}>
                Logout
            </span>
        </Nav>
    );
}

/**
 * Navigation for Doctors
 * Shows profile, inbox, images, and patient list
 */
function OtherStaffNav({ onLogout, userInfo }) {
    return (
        <Nav className="ms-auto">
            <span className="nav-link user-info">
                User Info: {userInfo?.firstName || userInfo?.username || 'Staff'}
            </span>
            <Link to="/dashboard" className="nav-link">Dashboard</Link>
            <Link to="/profile" className="nav-link">My Profile</Link>
            <Link to="/inbox" className="nav-link">Inbox</Link>
            <Link to="/image/list" className="nav-link">Image</Link>
            <span onClick={onLogout} className="nav-link logout-nav-link" style={{ cursor: 'pointer' }}>
                Logout
            </span>
        </Nav>
    );
}

/**
 * Main Navigation Bar Component
 * Renders different navigation based on authentication status and user role
 */
function NavBar() {
    const { isLoggedIn, role, logout, loading, userInfo } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
    };

    // Show loading state while authentication is being checked
    if (loading) {
        return (
            <Navbar className="custom-navbar" expand="lg">
                <Container>
                    <Navbar.Brand as={Link} to="/">Journalsystem</Navbar.Brand>
                    <Nav className="ms-auto">
                        <span className="nav-link">Loading...</span>
                    </Nav>
                </Container>
            </Navbar>
        );
    }

    return (
        <Navbar className="custom-navbar" expand="lg">
            <Container>
                <Navbar.Brand as={Link} to="/">Journalsystem</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        {isLoggedIn && (role === 'doctor' || role === 'other_staff') && (
                            <Link to="/patient" className="nav-link">Patients</Link>
                        )}
                        {isLoggedIn && role === 'patient' && (
                            <Link to={`/patient/${userInfo.userId}`} className="nav-link">My Records</Link>
                        )}
                    </Nav>
                    {isLoggedIn ? (
                        role === 'Doctor' || role === 'doctor' ? (
                            <DoctorNav onLogout={handleLogout} userInfo={userInfo} />
                        ) : role === 'Other_Staff' || role === 'other_staff' ? (
                            <OtherStaffNav onLogout={handleLogout} userInfo={userInfo} />
                        ) : (
                            <PatientNav onLogout={handleLogout} userInfo={userInfo} />
                        )
                    ) : (
                        <LoggedOutNav />
                    )}
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}

export default NavBar;
