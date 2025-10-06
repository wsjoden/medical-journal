/**
 * New Encounter Page
 * 
 * Form page for doctors and staff to create new encounters for patients.
 * 
 */

import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import '../styles/NewEncounterPage.css';
import { apiRequest } from '../services/RESTService';
import { useAuth } from '../services/AuthContext';

function NewEncounterPage() {
    const { id } = useParams();
    const { role } = useAuth();
    const [date, setDate] = useState('');
    const [reason, setReason] = useState('');
    const [notes, setNotes] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (e) => {
        e.preventDefault(); // Prevent page reload
        const encounterData = {
            encounterDate: date,
            reason,
            notes,
            patientId: id // Get id from URL
        };
        apiRequest('POST', `/encounters/new/patient/${id}`, encounterData)
            .then(response => {
                if (role === 'doctor') {
                    navigate(`/patient/${id}`); // Doctor go to patient profile
                } else {
                    navigate(`/patient`);   // Other staff go to patient list
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
