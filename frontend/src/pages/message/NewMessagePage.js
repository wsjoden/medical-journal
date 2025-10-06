/**
 * New Message Page
 * 
 * Form page for sending a new message to start a conversation.
 * After sending, redirects to the newly created conversation thread.
 * 
 */

import '../../styles/NewMessagePage.css';
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../services/AuthContext'; // Import the AuthContext
import { apiRequest } from '../../services/RESTService';

function NewMessagePage({ userId }) {
    const [recipient, setRecipient] = useState('');
    const [message, setMessage] = useState('');
    const navigate = useNavigate();

    // Fetch authentication status and token from AuthContext
    const { isLoggedIn } = useAuth();

    // Redirect to login if the user is not logged in
    useEffect(() => {
        if (!isLoggedIn) {
            navigate('/login');
        }
    }, [isLoggedIn, navigate]);

    /**
    * Sends new message and creates conversation
    * Redirects to conversation page after successful send
    */
    const handleSendMessage = (e) => {
        e.preventDefault();

        // Get the token from localStorage
        const token = localStorage.getItem('token');

        if (!token) {
            console.error("No token found. Please log in.");
            return;
        }

        // Prepare the message payload
        const messageData = { receiverUsername: recipient, message: message };

        // Send message - backend creates new conversation and returns conversation ID
        apiRequest('POST', '/messages/new', messageData, token)
            .then(response => {
                // Redirect to the conversation page on success
                navigate(`/conversation/${response.data.conversationId}`);
            })
            .catch(error => {
                console.error('Error sending message:', error);
            });
    };

    return (
        <div className="new-message-page">
            <h2>Compose Message</h2>

            <form onSubmit={handleSendMessage}>
                <div>
                    <label htmlFor="recipient">Recipient</label>
                    <input
                        type="text"
                        id="recipient"
                        value={recipient}
                        onChange={(e) => setRecipient(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="message">Message</label>
                    <textarea
                        id="message"
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Send Message</button>
            </form>
        </div>
    );
}

export default NewMessagePage;