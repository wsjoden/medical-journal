import React, { useState } from 'react';
import { faMagnifyingGlass } from '@fortawesome/free-solid-svg-icons';
import '../styles/PatientSearchBar.css';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

function PatientSearchBar({ onSearch, onAdvancedSearch }) {
    const [searchTerm, setSearchTerm] = useState('');
    const [advancedSearch, setAdvancedSearch] = useState(false);
    const [filters, setFilters] = useState({
        // Patient search fields
        firstName: '',
        lastName: '',
        diagnose: '',
        observation: '',
        encounterDate: '',
        // Doctor & Staff search fields
        staffFirstName: '',
        staffLastName: '',
        staffUserName: '',
        // Multiple fields
        genericSearch: ''

    });

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFilters((prevFilters) => ({
            ...prevFilters,
            [name]: value
        }));
    };

    const handleSearch = () => {
        if (advancedSearch) {
            console.log('Advanced search filters:', filters);
            onAdvancedSearch(filters); // Call the advanced search function with filters
        } else {
            onSearch(searchTerm); // Call the basic search function with searchTerm
        }
    };

    // Handle Enter key press for triggering search
    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    };

    const clearAdvancedFilters = () => {
        setFilters({
            firstName: '',
            lastName: '',
            diagnose: '',
            observation: '',
            encounterDate: '',
            staffUserName: '',
            staffFirstName: '',
            staffLastName: '',
            genericSearch: ''
        });
    };

    return (
        <div className="search-bar">
            <div className="basic-search">
                <div className="search-input-container">
                    <input
                        type="text"
                        name="basicSearch"
                        placeholder="Search for patients by name..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onKeyDown={handleKeyPress}  // Trigger search on Enter key press
                    />
                    <button onClick={handleSearch} className="search-icon">
                        <FontAwesomeIcon icon={faMagnifyingGlass} />
                    </button>
                </div>
            </div>

            <button
                onClick={() => setAdvancedSearch(!advancedSearch)}
                className="advanced-search-button">
                Advanced Search
            </button>

            {advancedSearch && (
                <div className="advanced-search-fields">
                    <h4>Patient Search</h4>
                    <div className="search-input-container">
                        <input
                            type="text"
                            name="firstName"
                            placeholder="Patient first name..."
                            value={filters.firstName}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress}
                        />
                        <input
                            type="text"
                            name="lastName"
                            placeholder="Patient last name..."
                            value={filters.lastName}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress}
                        />
                    </div>
                    <div className="search-input-container">
                        <input
                            type="text"
                            name="diagnose"
                            placeholder="Diagnose..."
                            value={filters.diagnose}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress}
                        />
                        <input
                            type="text"
                            name="observation"
                            placeholder="observation..."
                            value={filters.observation}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress}
                        />
                    </div>

                    <div>
                        <h4>Medical Records Search</h4>
                        <div className="search-input-container">
                            <div className="date-input-group">
                                <label htmlFor="encounterDate">Encounter Date:</label>
                                <input
                                    type="date"
                                    name="encounterDate"
                                    id="encounterDate"
                                    value={filters.encounterDate}
                                    onChange={handleInputChange}
                                    onKeyDown={handleKeyPress}
                                    className="encounter-date-input"
                                />
                            </div>
                        </div>
                    </div>

                    <h4>Search by Doctor/Staff</h4>
                    <div className="search-input-container">
                        <input
                            type="text"
                            name="staffUserName"
                            placeholder='Doctor/Staff Username...'
                            value={filters.staffUserName}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress}
                        />
                    </div>
                    <div className="search-input-container">
                        <input
                            type="text"
                            name="staffFirstName"
                            placeholder="Doctor/Staff first name..."
                            value={filters.staffFirstName}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress}
                        />
                        <input
                            type="text"
                            name="staffLastName"
                            placeholder="Doctor/Staff last name..."
                            value={filters.staffLastName}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress}
                        />
                    </div>

                    <h4>Generic Search</h4>
                    <div className="search-input-container">
                        <input
                            type="text"
                            name="genericSearch"
                            placeholder="Search across all fields..."
                            value={filters.genericSearch}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress}
                        />
                    </div>

                    <div className="search-buttons">
                        <button onClick={handleSearch} className="search-button">
                            Search
                        </button>
                        <button onClick={clearAdvancedFilters} className="clear-button">
                            Clear Filters
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default PatientSearchBar;
