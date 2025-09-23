import React, { useEffect, useState } from 'react';
import { apiRequest, apiRequestBlob } from "../../services/RESTService";
import { Link } from "react-router-dom";

const UploadedImagesList = () => {
    const [files, setFiles] = useState([]);
    const [imageUrls, setImageUrls] = useState({});
    const [loadingImages, setLoadingImages] = useState({});

    // Fetch Image files
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

    // Load images when file state changes.
    useEffect(() => {
        files.forEach(file => {
            if (file.mimetype && file.mimetype.startsWith('image/')) {
                loadImage(file.filename);
            }
        });
    }, [files]);

    // Memory Cleanup
    useEffect(() => {
        return () => {
            Object.values(imageUrls).forEach(url => {
                window.URL.revokeObjectURL(url);
            });
        };
    }, []);

    const loadImage = async (filename) => {
        if (imageUrls[filename] || loadingImages[filename]) return;

        console.log(`Loading image: ${filename}`); // Debug which image is being loaded
        setLoadingImages(prev => ({ ...prev, [filename]: true }));

        try {
            const response = await apiRequestBlob('GET', `/images/download/${filename}`);

            console.log('Response status:', response.status);
            console.log('Response headers:', response.headers);
            console.log('Response data type:', typeof response.data);
            console.log('Response data size:', response.data?.size || 'unknown');

            // Validate response
            if (!response.data) {
                throw new Error('No data received from server');
            }

            if (response.data.size === 0) {
                throw new Error('Received empty file');
            }

            const url = window.URL.createObjectURL(response.data);
            console.log(`Created blob URL for ${filename}:`, url); // Debug confirm blob creation

            setImageUrls(prev => ({ ...prev, [filename]: url }));
        } catch (error) {
            console.error(`Failed to load image ${filename}:`, error);
            console.error('Error response:', error.response?.data);
            console.error('Error status:', error.response?.status);
            console.error('Full error object:', error);
        } finally {
            setLoadingImages(prev => ({ ...prev, [filename]: false }));
        }
    };

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
                                {loadingImages[file.filename] ? (
                                    <div style={{ height: '150px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                        Loading...
                                    </div>
                                ) : imageUrls[file.filename] ? (
                                    <img
                                        src={imageUrls[file.filename]}
                                        alt={file.name}
                                        style={{
                                            width: '100%',
                                            maxHeight: '150px',
                                            objectFit: 'cover',
                                            marginBottom: '10px'
                                        }}
                                    />
                                ) : (
                                    <div>Failed to load image</div>
                                )}
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