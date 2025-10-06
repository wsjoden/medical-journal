import { apiRequest } from "./RESTService";

/**
 * Fetches search results using the `apiRequest` service.
 * @param {string} searchTerm - The basic search term.
 * @param {Object} filters - Advanced search filters.
 * @returns {Promise<Object>} - The search results.
 */
async function fetchSearchResults(searchTerm, filters = {}) {
    console.log('Searching for:', { searchTerm, filters });

    try {
        const queryParams = new URLSearchParams();

        // Use searchTerm as genericSearch if no specific genericSearch is provided
        if (searchTerm && !filters.genericSearch) {
            queryParams.append('genericSearch', searchTerm);
        }

        // Add all filter parameters if they exist
        if (filters.genericSearch) queryParams.append('genericSearch', filters.genericSearch);
        if (filters.firstName) queryParams.append('firstName', filters.firstName);
        if (filters.lastName) queryParams.append('lastName', filters.lastName);
        if (filters.diagnose) queryParams.append('diagnose', filters.diagnose);
        if (filters.encounterDate) queryParams.append('encounterDate', filters.encounterDate);
        if (filters.observation) queryParams.append('observation', filters.observation);
        if (filters.staffUserName) queryParams.append('staffUserName', filters.staffUserName);
        if (filters.staffFirstName) queryParams.append('staffFirstName', filters.staffFirstName);
        if (filters.staffLastName) queryParams.append('staffLastName', filters.staffLastName);

        const url = `/search?${queryParams.toString()}`;

        const response = await apiRequest('GET', url);

        return response.data;
    } catch (error) {
        console.error('Error fetching search results:', error);
        throw error;
    }
}

export { fetchSearchResults };
