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
    const [isEditing, setIsEditing] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        apiRequest('GET', '/user/profile', {})
            .then(response => {
                console.log("Profile data fetched successfully:", response.data); // Debugging: Log fetched profile data
                setProfile({
                    id: response.data.userId,
                    username: response.data.username,
                    role: response.data.role,
                    firstName: response.data.firstName,
                    lastName: response.data.lastName
                });
                console.log("Profile state after setProfile:", {
                    id: response.data.userId,
                    username: response.data.username,
                    role: response.data.role,
                    firstName: response.data.firstName,
                    lastName: response.data.lastName
                }); // Debugging: Log profile state after setting it
            })
            .catch(error => {
                console.error('Error fetching profile:', error); // Debugging: Log fetch errors
            });
    }, []);

    const handleDelete = () => {
        console.log("Deleting profile...");
        apiRequest('DELETE', `/user/${profile.id}`, {})
            .then(() => {
                console.log("Profile deleted successfully");
                navigate('/'); // Redirect to home or login page after deletion
            })
            .catch(error => {
                console.error('Error deleting profile:', error);
            });
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProfile(prevProfile => {
            const updatedProfile = {
                ...prevProfile,
                [name]: value || '',
            };
            console.log("Updated profile state:", updatedProfile); // Debugging: Log updated profile state
            return updatedProfile;
        });
    };

    const handleSave = (e) => {
        e.preventDefault();
        console.log("Saving profile data:", profile); // Debugging: Log data being saved
        apiRequest('PUT', `/user/profile/${profile.id}`, profile)
            .then(response => {
                console.log("Profile updated successfully:", response.data);
                setProfile(response.data); // Update with the latest data from the response
                setIsEditing(false);
            })
            .catch(error => {
                console.error('Error updating profile:', error);
            });
    };

    const handleEdit = () => {
        console.log("Entering edit mode...");
        setIsEditing(true);
    };

    const handleDetailsClick = () => {
        if (profile.id) {
            console.log("Navigating to details page for patient ID:", profile.id);
            navigate(`/patient/${profile.id}`);
        } else {
            console.error('Patient ID is undefined');
        }
    };

    return (
        <div className="profile-page">
            <div className="header">
                <h1>Profile</h1>
                {profile.role === 'patient' && (
                    <button className="button details-button" onClick={handleDetailsClick}>Details</button>
                )}
            </div>
            <form>
                <div className="form-group">
                    <label htmlFor="username" className="form-label">Username</label>
                    <input
                        type="text"
                        className="form-control"
                        id="username"
                        name="username"
                        value={profile.username || ''}
                        onChange={handleChange}
                        disabled={!isEditing}
                    />
                </div>
                {profile.role !== 'patient' && (
                    <div className="form-group">
                        <label htmlFor="role" className="form-label">Role</label>
                        <input
                            type="text"
                            className="form-control"
                            id="role"
                            name="role"
                            value={profile.role || ''}
                            onChange={handleChange}
                            disabled
                        />
                    </div>
                )}
                <div className="form-group">
                    <label htmlFor="firstName" className="form-label">First Name</label>
                    <input
                        type="text"
                        className="form-control"
                        id="firstName"
                        name="firstName"
                        value={profile.firstName || ''}
                        onChange={handleChange}
                        disabled={!isEditing}
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="lastName" className="form-label">Last Name</label>
                    <input
                        type="text"
                        className="form-control"
                        id="lastName"
                        name="lastName"
                        value={profile.lastName || ''}
                        onChange={handleChange}
                        disabled={!isEditing}
                    />
                </div>
                {isEditing ? (
                    <button type="button" className="button save-button" onClick={handleSave}>Save</button>
                ) : (
                    <button type="button" className="button edit-button" onClick={handleEdit}>Edit</button>
                )}
                <div>
                    <button type="button" className="button delete-button" onClick={handleDelete}>Delete</button>
                </div>
            </form>
        </div>
    );
}

export default ProfilePage;
