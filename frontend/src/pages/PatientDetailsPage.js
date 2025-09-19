import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import '../styles/PatientDetailsPage.css';
import { apiRequest } from '../services/RESTService';
import { useAuth } from '../services/AuthContext';

function PatientDetailsPage() {
    const { id } = useParams();
    const { user } = useAuth();
    const [patient, setPatient] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    // DEBUG LOGS - Add these
    console.log('=== PatientDetailsPage Auth Debug ===');
    console.log('user object:', user);
    console.log('user?.role:', user?.role);
    console.log('typeof user?.role:', typeof user?.role);
    console.log('user?.role === "doctor":', user?.role === 'doctor');
    console.log('user?.role === "Doctor":', user?.role === 'Doctor');
    console.log('user?.role === "other_staff":', user?.role === 'other_staff');
    console.log('user?.role === "Other_Staff":', user?.role === 'Other_Staff');

    useEffect(() => {
        apiRequest('GET', `/user/details/${id}`, {})
            .then(response => {
                console.log('Fetched patient data:', response.data);
                setPatient(response.data);
                setLoading(false);
            })
            .catch(error => {
                console.error('Error fetching patient details:', error);
                setError('Failed to fetch patient details.');
                setLoading(false);
            });
    }, [id]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    if (!patient) {
        return <div>No patient data available.</div>;
    }

    const handleNewObservationClick = () => {
        navigate(`/observation/new/patient/${id}`);
    };

    const handleNewEncounterClick = () => {
        navigate(`/encounter/new/patient/${id}`);
    };

    const handleNewDiagnoseClick = () => {
        navigate(`/diagnose/new/patient/${id}`);
    };

    // Helper function to check if user can add medical records
    const canAddMedicalRecords = () => {
        const result = user?.role === 'doctor' || user?.role === 'Doctor' || user?.role === 'other_staff' || user?.role === 'Other_Staff';
        console.log('canAddMedicalRecords result:', result);
        return result;
    };

    return (
        <div className="patient-details-page">
            <h1>Patient Details</h1>
            <div className="patient-info">
                <h2>{`${patient.firstName} ${patient.lastName}`}</h2>
                <p><strong>SSN:</strong> {patient.ssn}</p>
                <p><strong>Email:</strong> {patient.email}</p>
                <div className="current-diagnose">
                    <h3>Current Diagnose</h3>
                    {patient.currentDiagnose ? (
                        <>
                            <p><strong>Details:</strong> {patient.currentDiagnose}</p>
                        </>
                    ) : (
                        <p>No current diagnose available.</p>
                    )}
                </div>
            </div>

            <div className="patient-observations">
                <h2>Observations</h2>
                {patient.observationList && patient.observationList.length > 0 ? (
                    patient.observationList.map((observation, index) => (
                        <div key={index} className="observation">
                            <p><strong>Date:</strong> {observation.observationDate}</p>
                            <p><strong>Details:</strong> {observation.observation}</p>
                        </div>
                    ))
                ) : (
                    <p>No observations found.</p>
                )}

                {/* TEST BUTTON - Always show for debugging */}
                <button onClick={handleNewObservationClick} className="btn btn-secondary mt-2">
                    Add New Observation (TEST - ALWAYS SHOW)
                </button>

                {/* Conditional button */}
                {canAddMedicalRecords() && (
                    <button onClick={handleNewObservationClick} className="btn btn-primary mt-3">
                        Add New Observation (CONDITIONAL)
                    </button>
                )}
            </div>

            <div className="patient-encounters">
                <h2>Encounters</h2>
                {patient.encounterList && patient.encounterList.length > 0 ? (
                    patient.encounterList.map((encounter, index) => (
                        <div key={index} className="encounter">
                            <p><strong>Date:</strong> {encounter.encounterDate}</p>
                            <p><strong>Details:</strong> {encounter.notes}</p>
                        </div>
                    ))
                ) : (
                    <p>No encounters found.</p>
                )}
                {canAddMedicalRecords() && (
                    <button onClick={handleNewEncounterClick} className="btn btn-primary mt-3">
                        Add New Encounter
                    </button>
                )}
            </div>

            <div className="diagnose-history">
                <h2>Diagnose History</h2>
                {patient.diagnoseList && patient.diagnoseList.length > 0 ? (
                    patient.diagnoseList.map((diagnose, index) => (
                        <div key={index} className="diagnose">
                            <p><strong>Date:</strong> {diagnose.diagnosisDate}</p>
                            <p><strong>Details:</strong> {diagnose.diagnose}</p>
                        </div>
                    ))
                ) : (
                    <p>No diagnose history found.</p>
                )}
                {canAddMedicalRecords() && (
                    <button onClick={handleNewDiagnoseClick} className="btn btn-primary mt-3">
                        Add New Diagnose
                    </button>
                )}
            </div>
        </div>
    );
}

export default PatientDetailsPage;