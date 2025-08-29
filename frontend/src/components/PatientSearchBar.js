import React, { useState } from 'react';
import { faMagnifyingGlass } from '@fortawesome/free-solid-svg-icons';
import '../styles/PatientSearchBar.css';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

function PatientSearchBar({ onSearch, onAdvancedSearch }) {
    const [searchTerm, setSearchTerm] = useState('');
    const [advancedSearch, setAdvancedSearch] = useState(false);
    const [filters, setFilters] = useState({
        staffFirstName: '',
        staffLastName: '',
        staffEmail: '',
        staffSSN: '',
        diagnose: '',
        observation: '',
        encounterDate: ''
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

    return (
        <div className="search-bar">
            <div className="basic-search">
                <div className="search-input-container">
                    <input
                        type="text"
                        name="genericSearch"
                        placeholder="Search for patients..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onKeyDown={handleKeyPress}  // Trigger search on Enter key press
                    />
                    <button onClick={handleSearch} className="search-icon">
                        <FontAwesomeIcon icon={faMagnifyingGlass}/>
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
                    <div className="search-input-container">
                        <input
                            type="text"
                            name="staffFirstName"
                            placeholder="Staff first name..."
                            value={filters.staffFirstName}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress} // Trigger search when Enter is pressed in advanced fields
                        />
                        <input
                            type="text"
                            name="staffLastName"
                            placeholder="Staff last name..."
                            value={filters.staffLastName}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress} // Trigger search when Enter is pressed in advanced fields
                        />
                    </div>
                    <div className="search-input-container">
                        <input
                            type="text"
                            name="staffEmail"
                            placeholder="Staff email..."
                            value={filters.staffEmail}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress} // Trigger search when Enter is pressed in advanced fields
                        />
                        <input
                            type="text"
                            name="staffSSN"
                            placeholder="Staff SSN..."
                            value={filters.staffSSN}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress} // Trigger search when Enter is pressed in advanced fields
                        />
                    </div>
                    <div className="search-input-container">
                        <input
                            type="text"
                            name="diagnose"
                            placeholder="Diagnose..."
                            value={filters.diagnose}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress} // Trigger search when Enter is pressed in advanced fields
                        />
                        <input
                            type="text"
                            name="observation"
                            placeholder="Observation..."
                            value={filters.observation}
                            onChange={handleInputChange}
                            onKeyDown={handleKeyPress} // Trigger search when Enter is pressed in advanced fields
                        />
                    </div>
                    <p className="encounter-date-label">Encounter date</p>
                    <input
                        type="date"
                        name="encounterDate"
                        placeholder="Encounter Date"
                        value={filters.encounterDate}
                        onChange={handleInputChange}
                        onKeyDown={handleKeyPress} // Trigger search when Enter is pressed in advanced fields
                        className="encounter-date-input"
                    />
                </div>
            )}
        </div>
    );
}

export default PatientSearchBar;
