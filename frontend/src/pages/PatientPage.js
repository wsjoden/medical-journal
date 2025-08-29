import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiRequest } from '../services/RESTService';
import PatientSearchBar from '../components/PatientSearchBar';
import '../styles/PatientPage.css';
import {fetchSearchResults} from "../services/SearchService";

function PatientPage() {
    const [patients, setPatients] = useState([]);
    const [filteredPatients, setFilteredPatients] = useState([]);
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [dropdownVisible, setDropdownVisible] = useState(null);

    useEffect(() => {
        apiRequest("GET", "/user/patients", {})
            .then((response) => {

                console.log('Fetched patients:', response.data);

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

    const toggleDropdown = (index) => {
        setDropdownVisible(dropdownVisible === index ? null : index);
    };

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

    const handleSearch = (searchTerm) => {
        const term = searchTerm.toLowerCase();
        const filtered = patients.filter(patient =>
            patient.firstName.toLowerCase().includes(term) ||
            patient.lastName.toLowerCase().includes(term) ||
            patient.ssn.includes(term)
        );
        setFilteredPatients(filtered);
    };

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
                                {patient.ssn} - {patient.lastName}, {patient.firstName}
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
