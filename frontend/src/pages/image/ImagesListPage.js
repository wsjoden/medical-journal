import React, { useEffect, useState } from 'react';
import { apiRequest } from "../../services/RESTService";
import { Link } from "react-router-dom";

const UploadedImagesList = () => {
    const [files, setFiles] = useState([]);

    const getImageUrl = (filename) => {
        const baseURL = process.env.REACT_APP_IMAGE_SERVICE_URL || 'https://medical-app-image-service.app.cloud.cbh.kth.se';
        return `${baseURL}/images/download/${filename}`;
    };

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
            <div style={{ display: 'grid', gap: '15px', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))' }}>
                {files.map((file, index) => (
                    <div key={index} style={{ border: '1px solid #ddd', padding: '15px', borderRadius: '8px' }}>
                        <h4>{file.name || file.originalname}</h4>
                        <p><strong>Uploaded by:</strong> {file.uploadedBy}</p>
                        <p><strong>Date:</strong> {new Date(file.uploadedAt).toLocaleDateString()}</p>

                        {file.mimetype && file.mimetype.startsWith('image/') ? (
                            <div>
                                <img
                                    src={getImageUrl(file.filename)}
                                    alt={file.name}
                                    style={{
                                        width: '100%',
                                        maxHeight: '150px',
                                        objectFit: 'cover',
                                        marginBottom: '10px'
                                    }}
                                />
                                <a
                                    href={getImageUrl(file.filename)}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    style={{ color: '#007bff', textDecoration: 'none' }}
                                >
                                    View Full Size
                                </a>
                            </div>
                        ) : (
                            <p>File: {file.originalname}</p>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default UploadedImagesList;