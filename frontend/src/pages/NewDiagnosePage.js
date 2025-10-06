/**
 * New Diagnosis Page
 * 
 * Form page for doctors and staff to create new diagnoses for patients.
 * 
 */

import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import '../styles/NewDiagnosePage.css';
import { apiRequest } from '../services/RESTService';
import { useAuth } from '../services/AuthContext';

function NewDiagnosePage() {
    const { id } = useParams(); // Extract patient ID from URL
    const { role } = useAuth();
    const [diagnose, setDiagnose] = useState('');
    const [details, setDetails] = useState('');
    const navigate = useNavigate();

    /**
     * Handles form submission to create new diagnosis
     * Redirects based on role after successful creation
     */
    const handleSubmit = (e) => {
        e.preventDefault(); // Prevent page reload
        const diagnosisData = {
            diagnose,
            details,
            patientId: id   // Get id from URL
        };
        apiRequest('POST', `/diagnoses/new/patient/${id}`, diagnosisData)
            .then(response => {
                if (role === 'doctor') {
                    navigate(`/patient/${id}`); // Doctor go to patient profile
                } else {
                    navigate(`/patient`);   // Other staff go to patient list
                }
            })
            .catch(error => {
                console.error('Error creating diagnosis:', error);
            });
    };

    return (
        <div className="new-diagnos-page">
            <h1>New Diagnosis</h1>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="diagnosis" className="form-label">Diagnosis</label>
                    <input
                        type="text"
                        className="form-control"
                        id="diagnosis"
                        value={diagnose}
                        onChange={(e) => setDiagnose(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="details" className="form-label">Details</label>
                    <textarea
                        className="form-control"
                        id="details"
                        value={details}
                        onChange={(e) => setDetails(e.target.value)}
                        required
                    ></textarea>
                </div>
                <button type="submit" className="button submit-button">Create Diagnosis</button>
            </form>
        </div>
    );
}

export default NewDiagnosePage;
