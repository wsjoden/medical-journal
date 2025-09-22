import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import '../styles/NewDiagnosePage.css';
import { apiRequest } from '../services/RESTService';
import { useAuth } from '../services/AuthContext';

function NewDiagnosePage() {
    const { id } = useParams();
    const { role } = useAuth();
    const [diagnose, setDiagnose] = useState('');
    const [details, setDetails] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (e) => {
        e.preventDefault();
        const diagnosisData = { diagnose, details, patientId: id };
        apiRequest('POST', `/diagnoses/new/patient/${id}`, diagnosisData)
            .then(response => {
                console.log('Diagnosis created:', response.data);
                if (role === 'doctor') {
                    navigate(`/patient/${id}`);
                } else {
                    navigate(`/patient`);
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
