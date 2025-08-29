import React, { useEffect, useState } from 'react';
import { apiRequest } from "../../services/RESTService";
import {Link} from "react-router-dom";

const UploadedImagesList = () => {
    const [files, setFiles] = useState([]);

    useEffect(() => {
        const fetchFiles = async () => {
            try {
                const response = await apiRequest('GET', '/images/list');
                setFiles(response.data);
            } catch (error) {
                console.error('Error fetching files:', error);
            }
        };

        fetchFiles();
    }, []);

    return (
        <div>
            <h2>Uploaded Files</h2>
            {/* Upload Button - Redirects to the Upload page */}
            <div style={{ marginBottom: '20px' }}>
                <Link to="/image/upload">
                    <button>Upload New Image</button>
                </Link>
            </div>
            {/* Edit Button - Redirects to the Edit page */}
            <div style={{ marginBottom: '20px' }}>
                <Link to="/image/edit">
                    <button>Edit Image</button>
                </Link>
            </div>
            <ul>
                {files.map((file, index) => (
                    <li key={index}>
                        <a href={`/uploads/${file.filename}`} target="_blank" rel="noopener noreferrer">
                            {file.originalname}
                        </a> - Uploaded by: {file.name}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default UploadedImagesList;