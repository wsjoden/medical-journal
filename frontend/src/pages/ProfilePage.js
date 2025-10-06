/**
 * Profile Page where users can view basic information about themselves
 * 
 */

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/ProfilePage.css';
import { apiRequest } from '../services/RESTService';

function ProfilePage() {
    const [profile, setProfile] = useState({
        id: '',
        username: '',
        role: '',
        firstName: '',
        lastName: ''
    });
    const navigate = useNavigate();

    // Fetches current user's profile on mount
    useEffect(() => {
        apiRequest('GET', '/user/profile', {})
            .then(response => {
                setProfile({
                    id: response.data.userId,
                    username: response.data.username,
                    role: response.data.role,
                    firstName: response.data.firstName,
                    lastName: response.data.lastName
                });
            })
            .catch(error => {
                console.error('Error fetching profile:', error);
            });
    }, []);

    const handleDetailsClick = () => {
        if (profile.id) {
            navigate(`/patient/${profile.id}`);
        } else {
            console.error('Patient ID is undefined');
        }
    };

    return (
        <div className="profile-page">
            <div className="header">
                <h1>Profile</h1>
                {/* Show Details button only for patients */}
                {profile.role === 'patient' && (
                    <button className="button details-button" onClick={handleDetailsClick}>
                        Details
                    </button>
                )}
            </div>

            <div className="profile-info">
                <div className="form-group">
                    <label className="form-label">Username</label>
                    <p className="form-value">{profile.username || 'N/A'}</p>
                </div>

                {profile.role !== 'patient' && (
                    <div className="form-group">
                        <label className="form-label">Role</label>
                        <p className="form-value">{profile.role || 'N/A'}</p>
                    </div>
                )}

                <div className="form-group">
                    <label className="form-label">First Name</label>
                    <p className="form-value">{profile.firstName || 'N/A'}</p>
                </div>

                <div className="form-group">
                    <label className="form-label">Last Name</label>
                    <p className="form-value">{profile.lastName || 'N/A'}</p>
                </div>
            </div>
        </div>
    );
}


export default ProfilePage;
