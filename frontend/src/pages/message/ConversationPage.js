import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import '../../styles/ConversationPage.css';
import { apiRequest } from '../../services/RESTService';

function ConversationPage() {
    const { id } = useParams();
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');

    useEffect(() => {
        apiRequest('GET', `/messages/conversation/${id}/messages`, {})
            .then(response => {
                console.log('Response from server:', response.data); // Logging the response
                setMessages(response.data);
            })
            .catch(error => {
                console.error('Error fetching messages:', error);
            });
    }, [id]);

    const handleSendMessage = (e) => {
        e.preventDefault();
        const messageData = {
            message: newMessage,
            receiverUsername: messages.length > 0 ? messages[0].receiverUsername : '', // Assuming the receiver is the same for all messages in the conversation
        };
        console.log('Sending message data:', messageData); // Log the message data
        apiRequest('POST', `/messages/reply/${id}`, messageData)
            .then(response => {
                console.log('Message sent response:', response.data); // Logging the response
                setMessages(response.data);
                setNewMessage('');
            })
            .catch(error => {
                console.error('Error sending message:', error);
            });
    };

    const formatDate = (dateString) => {
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        return new Date(dateString).toLocaleDateString(undefined, options);
    };

    return (
        <div>
            <div className="conversation-page">
                <div className="chat-container">
                    <div className="chat-header">
                        <h2>Conversation</h2>
                    </div>
                    <div className="chat-messages">
                        {messages.length > 0 ? (
                            messages.map((message, index) => (
                                <div key={index} className="message">
                                    <div className="message-sender">From: {message.senderUsername}</div>
                                    <div className="message-receiver">To: {message.receiverUsername}</div>
                                    <div className="message-text">Message: {message.message}</div>
                                    <div className="message-timestamp">{formatDate(message.sentDate)}</div>
                                </div>
                            ))
                        ) : (
                            <p>No messages yet.</p>
                        )}
                    </div>
                    <form className="chat-input-form" onSubmit={handleSendMessage}>
                        <textarea
                            className="chat-input"
                            id="newMessage"
                            value={newMessage}
                            onChange={(e) => setNewMessage(e.target.value)}
                            placeholder="Type your message here..."
                            required
                        />
                        <button className="send-button" type="submit">Send Message</button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default ConversationPage;