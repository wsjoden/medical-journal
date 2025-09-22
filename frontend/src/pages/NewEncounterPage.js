import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import '../styles/NewEncounterPage.css';
import { apiRequest } from '../services/RESTService';

function NewEncounterPage() {
    const { id } = useParams();
    const [date, setDate] = useState('');
    const [reason, setReason] = useState('');
    const [notes, setNotes] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (e) => {
        e.preventDefault();
        const encounterData = { encounterDate: date, reason, notes, patientId: id };
        console.log('Encounter Data:', encounterData); // Log the data to check the format
        apiRequest('POST', `/encounters/new/patient/${id}`, encounterData)
            .then(response => {
                console.log('Encounter created:', response.data);
                if (role === 'doctor') {
                    navigate(`/patient/${id}`);
                } else {
                    navigate(`/patient`);
                }
            })
            .catch(error => {
                console.error('Error creating encounter:', error);
            });
    };

    return (
        <div className="new-encounter-page">
            <h1>New Encounter</h1>
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
                    <label htmlFor="notes" className="form-label">Notes</label>
                    <textarea
                        className="form-control"
                        id="notes"
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                        required
                    ></textarea>
                </div>
                <button type="submit" className="button submit-button">Create Encounter</button>
            </form>
        </div>
    );
}

export default NewEncounterPage;
