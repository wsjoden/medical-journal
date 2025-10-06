/**
 * Home Page
 * 
 * Can be access without authentication
 * 
 */


import React from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from "../services/RESTService";
import '../styles/HomePage.css';

function HomePage() {

    const [message, setMessage] = React.useState('');

    // Test to that let you ping the backend microservices
    const handleServiceTest = (url) => {
        apiRequest('get', url)
            .then(response => {
                setMessage(response.data);
            })
            .catch(error => {
                console.error("Error calling the service:", error);
                setMessage("Failed to reach the service.");
            });
    };

    return (
        <div className="home-page">
            <header className="header">
                <h1>Welcome to the Patient Journal App</h1>
                <p>Created by William Sjödén for KTH Fullstack Course</p>
            </header>
            <nav className="nav">
                <ul>
                    <li><Link to="#about">About</Link></li>
                    <li><Link to="#features">Features</Link></li>
                    <li><Link to="#contact">Contact</Link></li>
                </ul>
            </nav>
            <main className="main">
                <section id="about" className="section">
                    <h2>About</h2>
                    <p>This app helps you manage patient records efficiently and securely.</p>
                </section>
                <section id="features" className="section">
                    <h2>Features</h2>
                    <ul>
                        <li>Login and user creation with three types of users: patient, doctor, and other staff.</li>
                        <li>Doctors and other staff can create new patient notes and diagnose patients.</li>
                        <li>Doctors can view all information for a single patient.</li>
                        <li>Patients can view all their own information.</li>
                        <li>Patients can send messages to doctors or other staff and see responses.</li>
                        <li>Doctors and other staff can view and respond to messages.</li>
                    </ul>
                </section>
            </main>
            <footer className="footer">
                <p>© 2024 William Sjödén. All rights reserved.</p>

                <div className="service-test-buttons">
                    <button onClick={() => handleServiceTest("/user/test")}>User Service Test</button>
                    <button onClick={() => handleServiceTest("/messages/test")}>Message Service Test</button>
                    <button onClick={() => handleServiceTest("/jwt/test")}>JWT Service Test</button>
                    <button onClick={() => handleServiceTest("/diagnoses/test")}>Medical Service Test</button>
                    <button onClick={() => handleServiceTest("/images/test")}>Image Service Test</button>
                    <button onClick={() => handleServiceTest("/search/test")}>Search Service Test</button> {/* New button for SearchService */}
                </div>

                <div className="service-message">
                    {message && <p>{message}</p>}
                </div>
            </footer>
        </div>
    );
}

export default HomePage;
