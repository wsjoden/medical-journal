import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styles/InboxPage.css';
import { apiRequest } from '../../services/RESTService';

function InboxPage() {
    const [conversations, setConversations] = useState([]);
    const navigate = useNavigate();

    const handleNewMessageClick = () => {
        navigate('/message/new');
    }

    useEffect(() => {
        apiRequest('GET', '/messages/inbox', {})
            .then(response => {
                setConversations(response.data);
            })
            .catch(error => {
                console.error('Error fetching conversations:', error);
            });
    }, []);

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
                            {conversation.user1} & {conversation.user2}
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