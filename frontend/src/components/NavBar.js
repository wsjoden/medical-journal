import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container } from 'react-bootstrap';
import { useAuth } from "../services/AuthContext";
import '../styles/NavBar.css';


function LoggedOutNav() {
    const { login } = useAuth();

    const handleLoginClick = (e) => {
        e.preventDefault();
        login();
    };

    return (
        <Nav className="ms-auto">
            <span onClick={handleLoginClick} className="nav-link login-nav-link" style={{ cursor: 'pointer' }}>
                Login / Register
            </span>
        </Nav>
    );
}

function PatientNav({ onLogout, userInfo }) {
    return (
        <Nav className="ms-auto">
            <span className="nav-link user-info">
                User Info: {userInfo?.firstName || userInfo?.username || 'Patient'}
            </span>
            <Link to="/profile" className="nav-link">My Profile</Link>
            <Link to="/inbox" className="nav-link">Inbox</Link>
            <Link to="/image/list" className="nav-link">Images</Link>
            <span onClick={onLogout} className="nav-link logout-nav-link" style={{ cursor: 'pointer' }}>
                Logout
            </span>
        </Nav>
    );
}

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

function NavBar() {
    const { isLoggedIn, role, logout, loading, userInfo } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
    };

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

    console.log('NavBar - Role from AuthContext:', role);
    console.log('NavBar - Is logged in:', isLoggedIn);

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
                            <Link to="/my-records" className="nav-link">My Records</Link>
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
