import axios from "axios";

const defaultBaseURL = process.env.REACT_APP_DEFAULT_URL || "http://localhost:8080";
const jwtBaseURL = process.env.REACT_APP_JWT_URL || "http://localhost:8081";
const userBaseURL = process.env.REACT_APP_USER_SERVICE_URL || "http://localhost:8082";
const messagesBaseURL = process.env.REACT_APP_MESSAGE_SERVICE_URL || "http://localhost:8083";
const medicalDataBaseURL = process.env.REACT_APP_MEDICAL_DATA_SERVICE_URL || "http://localhost:8084";
const imageBaseURL = process.env.REACT_APP_IMAGE_SERVICE_URL || "http://localhost:8085";
const searchBaseURL = process.env.REACT_APP_SEARCH_SERVICE_URL || "http://localhost:8086";

axios.defaults.headers.post["Content-Type"] = "application/json";

export const apiRequest = (method, url, data) => {
    const token = localStorage.getItem('token');
    console.log("Token from localStorage:", localStorage.getItem('token'));

    let baseURL = defaultBaseURL;
    if (url.startsWith("/user") || url.startsWith("/patient") || url.startsWith("/staff")) {
        baseURL = userBaseURL;
    } else if (url.startsWith("/jwt")) {
        baseURL = jwtBaseURL;
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

export const apiRequestFile = (method, url, formData) => {
    const token = localStorage.getItem('token');
    console.log("Token from localStorage:", localStorage.getItem('token'));

    let baseURL = defaultBaseURL;
    if (url.startsWith("/user") || url.startsWith("/patient") || url.startsWith("/staff")) {
        baseURL = userBaseURL;
    } else if (url.startsWith("/jwt")) {
        baseURL = jwtBaseURL;
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
        data: formData,
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
};
