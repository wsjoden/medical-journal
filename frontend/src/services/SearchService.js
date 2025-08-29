import { apiRequest } from "./RESTService";

/**
 * Construct a search URL based on the search parameters & filters
 * @param {string} searchTerm - The search term to use
 * @param {object} filters - The filters to apply
 * @returns {string} - The constructed URL
 */
function constructSearchURL(searchTerm, filters) {
    console.log("Entered constructSearchURL function");
    let url = `/search?q=${encodeURIComponent(searchTerm)}`;

    if (filters.genericSearch) url += `&genericSearch=${encodeURIComponent(filters.genericSearch)}`;
    if (filters.staffFirstName) url += `&staffFirstName=${encodeURIComponent(filters.staffFirstName)}`;
    if (filters.staffLastName) url += `&staffLastName=${encodeURIComponent(filters.staffLastName)}`;
    if (filters.staffEmail) url += `&staffEmail=${encodeURIComponent(filters.staffEmail)}`;
    if (filters.staffSSN) url += `&staffSSN=${encodeURIComponent(filters.staffSSN)}`;
    if (filters.encounterDate) url += `&encounterDate=${encodeURIComponent(filters.encounterDate)}`;
    if (filters.observation) url += `&observation=${encodeURIComponent(filters.observation)}`;
    if (filters.diagnose) url += `&diagnose=${encodeURIComponent(filters.diagnose)}`;

    return url;
}

/**
 * Fetches search results using the `apiRequest` service.
 * @param {string} searchTerm - The basic search term.
 * @param {Object} filters - Advanced search filters.
 * @returns {Promise<Object>} - The search results.
 */
async function fetchSearchResults(searchTerm, filters = {}) {
    const url = constructSearchURL(searchTerm, filters);
    try {
        const response = await apiRequest('GET', url);
        return response.data;
    } catch (error) {
        console.error('Error fetching search results:', error);
        throw error;
    }
}

export { fetchSearchResults };
