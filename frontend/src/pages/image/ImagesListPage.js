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

        setLoadingImages(prev => ({ ...prev, [filename]: true }));

        try {
            const response = await apiRequestBlob('GET', `/images/download/${filename}`);
            const url = window.URL.createObjectURL(response.data);
            setImageUrls(prev => ({ ...prev, [filename]: url }));
        } catch (error) {
            console.error('Failed to load image:', error);
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