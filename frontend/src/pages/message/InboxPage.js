/**
 * Inbox Page
 * 
 * Main messaging inbox that displays all conversations for the logged-in user.
 * Clicking on a thread opens up the conversation
 */

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styles/InboxPage.css';
import { useAuth } from '../../services/AuthContext';
import { apiRequest } from '../../services/RESTService';

function InboxPage() {
    const [conversations, setConversations] = useState([]);
    const navigate = useNavigate();
    const { userInfo } = useAuth();

    // Navigates to new message creation page
    const handleNewMessageClick = () => {
        navigate('/message/new');
    }
    /**
         * Fetches all conversations for the logged-in user on mount
         * Backend returns conversations where user is participating
         */
    useEffect(() => {
        apiRequest('GET', '/messages/inbox', {})
            .then(response => {
                setConversations(response.data);
            })
            .catch(error => {
                console.error('Error fetching conversations:', error);
            });
    }, []);

    // Navigate to conversation
    const handleConversationClick = (conversationId) => {
        navigate(`/conversation/${conversationId}`);
    };

    return (
        <div className="inbox-page">
            <h2>Inbox</h2>
            <button onClick={handleNewMessageClick} className="new-message-button">New Message</button>
            <div className="conversations-list">
                {conversations.map((conversation, index) => (
                    <div key={index} className="conversation-item" onClick={() => handleConversationClick(conversation.id)}>
                        <div className="conversation-participants">
                            {userInfo?.userName} & {conversation.participant}
                        </div>
                        <div className="conversation-last-message">
                            {conversation.messages.length > 0 ? conversation.messages[conversation.messages.length - 1] : "No messages yet"}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default InboxPage;