import axios from "axios";

// URLs with local fallbacks
const defaultBaseURL = process.env.REACT_APP_DEFAULT_URL || "http://localhost:8080";
const userBaseURL = process.env.REACT_APP_USER_SERVICE_URL || "http://localhost:8082";
const messagesBaseURL = process.env.REACT_APP_MESSAGE_SERVICE_URL || "http://localhost:8083";
const medicalDataBaseURL = process.env.REACT_APP_MEDICAL_DATA_SERVICE_URL || "http://localhost:8084";
const imageBaseURL = process.env.REACT_APP_IMAGE_SERVICE_URL || "http://localhost:8085";
const searchBaseURL = process.env.REACT_APP_SEARCH_SERVICE_URL || "http://localhost:8086";

axios.defaults.headers.post["Content-Type"] = "application/json";

/**
 * Makes an authenticated API request to the appropriate microservice
 * Automatically routes to correct backend based on URL path
 * 
 * @param {string} method - HTTP method
 * @param {string} url - Endpoint path
 * @param {Object} data - Request body data
 * @returns {Promise} Axios response promise
 */

export const apiRequest = (method, url, data) => {
    const token = localStorage.getItem('token');
    console.log("Token from localStorage:", localStorage.getItem('token'));
    // Route to correct microservice based on URL
    let baseURL = defaultBaseURL;
    if (url.startsWith("/user") || url.startsWith("/patient") || url.startsWith("/staff")) {
        baseURL = userBaseURL;
    } else if (url.startsWith("/messages")) {
        baseURL = messagesBaseURL;
    } else if (url.startsWith("/diagnoses") || url.startsWith("/observation") || url.startsWith("/encounter")) {
        baseURL = medicalDataBaseURL;
    } else if (url.startsWith("/images")) {
        baseURL = imageBaseURL;
    } else if (url.startsWith("/search")) {
        baseURL = searchBaseURL;
    }


    const requestURL = url.startsWith("http") ? url : baseURL + url;
    console.log("Request URL:", requestURL);

    return axios({
        method: method,
        url: requestURL,
        data: data,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    });
};

/**
 * Makes an authenticated file upload request (multipart/form-data)
 * 
 * @param {string} method - HTTP method (typically POST or PUT)
 * @param {string} url - Endpoint path
 * @param {FormData} formData - FormData object containing files
 * @returns {Promise} Axios response promise
 */
export const apiRequestFile = (method, url, formData) => {
    const token = localStorage.getItem('token');
    console.log("Token from localStorage:", localStorage.getItem('token'));

    // Route to correct microservice based on URL
    let baseURL = defaultBaseURL;
    if (url.startsWith("/user") || url.startsWith("/patient") || url.startsWith("/staff")) {
        baseURL = userBaseURL;
    } else if (url.startsWith("/messages")) {
        baseURL = messagesBaseURL;
    } else if (url.startsWith("/diagnoses") || url.startsWith("/observation") || url.startsWith("/encounter")) {
        baseURL = medicalDataBaseURL;
    } else if (url.startsWith("/images")) {
        baseURL = imageBaseURL;
    } else if (url.startsWith("/search")) {
        baseURL = searchBaseURL;
    }

    const requestURL = url.startsWith("http") ? url : baseURL + url;
    console.log("Request URL:", requestURL);

    // Clean axios Instance to not convert file into string
    const axiosInstance = axios.create({
        transformRequest: [function (data) {
            // Don't transform FormData
            return data;
        }],
        transformResponse: [function (data) {
            return data;
        }]
    });

    return axiosInstance({
        method: method,
        url: requestURL,
        data: formData,
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
};

/**
 * Makes an authenticated request that returns binary large object (blob)
 * Use this for downloading images or files
 * 
 * @param {string} method - HTTP method (typically GET)
 * @param {string} url - Endpoint path
 * @returns {Promise} Axios response promise with blob data
 */
export const apiRequestBlob = (method, url) => {
    const token = localStorage.getItem('token');
    console.log("=== apiRequestBlob Debug ===");
    console.log("Original URL:", url);

    let baseURL = defaultBaseURL;
    if (url.startsWith("/images")) {
        baseURL = imageBaseURL;
        console.log("Using imageBaseURL:", baseURL);
    }

    const requestURL = url.startsWith("http") ? url : baseURL + url;
    console.log("Final Request URL:", requestURL);
    console.log("Token present:", !!token);

    return axios({
        method: method,
        url: requestURL,
        responseType: 'blob',   //Tells axios to expect binary data
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
};
