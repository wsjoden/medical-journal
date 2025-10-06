/**
 * Patient Page
 * 
 * Lists all patients for doctors and staff.
 * 
 * Uses SearchService for advanced searches across patients
 * 
 */

import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiRequest } from '../services/RESTService';
import PatientSearchBar from '../components/PatientSearchBar';
import '../styles/PatientPage.css';
import { fetchSearchResults } from "../services/SearchService";

function PatientPage() {
    const [patients, setPatients] = useState([]);   // Patients from backend
    const [filteredPatients, setFilteredPatients] = useState([]);   // Filtered results from search
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [dropdownVisible, setDropdownVisible] = useState(null);   // Tracks which dropdown is open=

    /**
   * Fetches all patients
   * Initial patient list shown before any search
   */
    useEffect(() => {
        apiRequest("GET", "/user/patients", {})
            .then((response) => {

                if (Array.isArray(response.data)) {
                    setPatients(response.data);
                    setFilteredPatients(response.data);
                } else {
                    console.error('Expected an array but got:', response.data);
                }
                setLoading(false);
            })
            .catch((error) => {
                console.error('Error fetching data:', error);
                setLoading(false);
            });
    }, []);


    /**
     * Toggle dropdown for quick actions on patient
     * Only one dropdown can be open at a time
     * @param {number} index - Index of patient in the list
     */
    const toggleDropdown = (index) => {
        setDropdownVisible(dropdownVisible === index ? null : index);
    };

    /**
    * Handles selection from quick action dropdown
    * and navigates to correct form creating page
    */
    const handleOptionSelect = (option, id) => {
        setDropdownVisible(null);
        switch (option) {
            case 'Encounter':
                navigate(`/encounter/new/patient/${id}`);
                break;
            case 'Observation':
                navigate(`/observation/new/patient/${id}`);
                break;
            case 'Diagnose':
                navigate(`/diagnose/new/patient/${id}`);
                break;
            default:
                break;
        }
    };

    /**
    * Handles basic search by patient name
    * Filters locally from loaded patients
    */
    const handleSearch = (searchTerm) => {
        const term = searchTerm.toLowerCase();
        const filtered = patients.filter(patient =>
            patient.firstName.toLowerCase().includes(term) ||
            patient.lastName.toLowerCase().includes(term)
        );
        setFilteredPatients(filtered);
    };

    /**
   * Handles advanced search with multiple filters
   * Calls SearchService to query backend with complex searches
   */
    const handleAdvancedSearch = async (filters) => {
        try {
            const searchResults = await fetchSearchResults("", filters);
            setFilteredPatients(searchResults);
        } catch (error) {
            console.error('Error fetching search results:', error);
        }
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <div className="patient-page">
            <h2>Patient List</h2>
            <PatientSearchBar onSearch={handleSearch} onAdvancedSearch={handleAdvancedSearch} />
            {filteredPatients.length > 0 ? (
                <div className="patient-list">
                    {filteredPatients.map((patient, index) => (
                        <div key={index} className="patient-row">
                            <Link to={`/patient/${patient.userId}`} className="patient-link">
                                {patient.lastName}, {patient.firstName}
                            </Link>
                            <button
                                onClick={() => toggleDropdown(index)}
                                className="btn view-details-btn"
                            >
                                +
                            </button>
                            {dropdownVisible === index && (
                                <div className="dropdown-menu">
                                    <button onClick={() => handleOptionSelect('Encounter', patient.userId)} className="dropdown-item">
                                        Encounter
                                    </button>
                                    <button onClick={() => handleOptionSelect('Observation', patient.userId)} className="dropdown-item">
                                        Observation
                                    </button>
                                    <button onClick={() => handleOptionSelect('Diagnose', patient.userId)} className="dropdown-item">
                                        Diagnose
                                    </button>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            ) : (
                <p>No patients found.</p>
            )}
        </div>
    );
}

export default PatientPage;
