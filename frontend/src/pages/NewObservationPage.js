import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import '../styles/NewObservationPage.css';
import { apiRequest } from '../services/RESTService';
import { useAuth } from '../services/AuthContext';

function NewObservationPage() {
    const { id } = useParams();
    const { role } = useAuth();
    const [date, setDate] = useState('');
    const [observation, setDetails] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (e) => {
        e.preventDefault();
        const observationData = { observationDate: date, observation, patientId: id };
        console.log('Observation data to send:', observationData);
        apiRequest('POST', `/observations/new/patient/${id}`, observationData)
            .then(response => {
                console.log('Observation created:', response.data);
                if (role === 'doctor') {
                    navigate(`/patient/${id}`);
                } else {
                    navigate(`/patient`);
                };
            })
            .catch(error => {
                console.error('Error creating observation:', error);
            });
    };

    return (
        <div className="new-observation-page">
            <h1>New Observation</h1>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="date" className="form-label">Date</label>
                    <input
                        type="date"
                        className="form-control"
                        id="date"
                        value={date}
                        onChange={(e) => setDate(e.target.value)}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="details" className="form-label">Details</label>
                    <textarea
                        className="form-control"
                        id="observation"
                        value={observation}
                        onChange={(e) => setDetails(e.target.value)}
                        required
                    ></textarea>
                </div>
                <button type="submit" className="button submit-button">Create Observation</button>
            </form>
        </div>
    );
}

export default NewObservationPage;
