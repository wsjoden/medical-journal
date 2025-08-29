import '../styles/RegisterPage.css';
import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../services/AuthContext';
import {apiRequest} from '../services/RESTService';

function RegisterPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [email, setEmail] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [ssn, setSSN] = useState('');
    const [role, setRole] = useState('Patient');
    const navigate = useNavigate();
    const {login} = useAuth();

    const handleRegister = (e) => {
        e.preventDefault();
        if (password === confirmPassword) {
            const userData = {username, password, email, firstName, lastName, ssn, role};
            apiRequest('POST', '/user/register', userData)
                .then(response => {
                    const token = response.data.token;
                    login(token)
                    navigate('/patient');
                })
                .catch(error => {
                    console.error('Error registering user:', error);
                });
        } else {
            alert('Passwords do not match');
        }
    };

    return (
        <div className="register-page">
            <div className="register-container">
                <h5 className="card-title">Register</h5>
                <form onSubmit={handleRegister}>
                    <div className="form-columns">
                        <div className="left-column">
                            <div className="form-group">
                                <label htmlFor="firstName" className="form-label">First Name</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    id="firstName"
                                    value={firstName}
                                    onChange={(e) => setFirstName(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="ssn" className="form-label">SSN</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    id="ssn"
                                    value={ssn}
                                    onChange={(e) => setSSN(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="username" className="form-label">Username</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    id="username"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="password" className="form-label">Password</label>
                                <input
                                    type="password"
                                    className="form-control"
                                    id="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </div>
                        </div>
                        <div className="right-column">
                            <div className="form-group">
                                <label htmlFor="lastName" className="form-label">Last Name</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    id="lastName"
                                    value={lastName}
                                    onChange={(e) => setLastName(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="email" className="form-label">Email</label>
                                <input
                                    type="email"
                                    className="form-control"
                                    id="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="role" className="form-label">Role</label>
                                <div className="select">
                                    <select
                                        className="form-control"
                                        id="role"
                                        value={role}
                                        onChange={(e) => setRole(e.target.value)}
                                        required
                                    >
                                        <option value="Patient">Patient</option>
                                        <option value="Doctor">Doctor</option>
                                        <option value="Other_Staff">Other Staff</option>
                                    </select>
                                    <span className="dropdown">&#9662;</span>
                                </div>
                            </div>
                            <div className="form-group">
                                <label htmlFor="confirmPassword" className="form-label">Confirm Password</label>
                                <input
                                    type="password"
                                    className="form-control"
                                    id="confirmPassword"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                />
                            </div>
                        </div>
                    </div>
                    <button type="submit" className="button register-button full-width">Register</button>
                </form>
            </div>
        </div>
    );
}

export default RegisterPage;
