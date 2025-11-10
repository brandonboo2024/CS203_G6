import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { chatbotService } from '../services/chatbotService';
import '../styles/Chatbot.css';

const Chatbot = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'bot',
      text: "Hello! I'm your TariffKey assistant. How can I help you today?",
      timestamp: new Date()
    }
  ]);
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef(null);
  const navigate = useNavigate();

  // Auto-scroll to bottom when new messages arrive
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Toggle chatbot window
  const toggleChat = () => {
    setIsOpen(!isOpen);
  };

  // Handle user message submission
  const handleSendMessage = (e) => {
    e.preventDefault();
    if (!inputMessage.trim()) return;

    // Add user message
    const userMessage = {
      id: messages.length + 1,
      sender: 'user',
      text: inputMessage,
      timestamp: new Date()
    };

    setMessages(prev => [...prev, userMessage]);
    setInputMessage('');
    setIsTyping(true);

    // Simulate typing delay for more natural interaction
    setTimeout(() => {
      // Process message with chatbot service
      const response = chatbotService.processMessage(inputMessage);

      // Add bot response
      const botMessage = {
        id: messages.length + 2,
        sender: 'bot',
        text: response.message,
        timestamp: new Date(),
        actions: response.actions || []
      };

      setMessages(prev => [...prev, botMessage]);
      setIsTyping(false);
    }, 500);
  };

  // Handle action button clicks
  const handleAction = (action) => {
    if (action.path) {
      // Navigate to specified path
      navigate(action.path);
      setIsOpen(false); // Close chatbot after navigation
    } else if (action.action === 'help') {
      // Show help
      const response = chatbotService.getHelpResponse();
      const botMessage = {
        id: messages.length + 1,
        sender: 'bot',
        text: response.message,
        timestamp: new Date(),
        actions: response.actions || []
      };
      setMessages(prev => [...prev, botMessage]);
    } else if (action.action?.startsWith('explain_')) {
      // Show explanation
      const response = chatbotService.getExplanation(action.action);
      const botMessage = {
        id: messages.length + 1,
        sender: 'bot',
        text: response.message,
        timestamp: new Date(),
        actions: response.actions || []
      };
      setMessages(prev => [...prev, botMessage]);
    } else if (action.action === 'export_history') {
      // Navigate to history page (export functionality is there)
      navigate('/history');
      setIsOpen(false);
    } else if (action.action === 'account_info') {
      // Navigate to profile
      navigate('/profile');
      setIsOpen(false);
    }
  };

  // Handle quick suggestion clicks
  const handleQuickSuggestion = (suggestion) => {
    setInputMessage(suggestion);
    // Auto-send the message
    const userMessage = {
      id: messages.length + 1,
      sender: 'user',
      text: suggestion,
      timestamp: new Date()
    };

    setMessages(prev => [...prev, userMessage]);
    setIsTyping(true);

    setTimeout(() => {
      const response = chatbotService.processMessage(suggestion);
      const botMessage = {
        id: messages.length + 2,
        sender: 'bot',
        text: response.message,
        timestamp: new Date(),
        actions: response.actions || []
      };

      setMessages(prev => [...prev, botMessage]);
      setIsTyping(false);
    }, 500);
  };

  // Clear conversation
  const handleClearChat = () => {
    setMessages([
      {
        id: 1,
        sender: 'bot',
        text: "Chat cleared. How can I help you?",
        timestamp: new Date()
      }
    ]);
  };

  // Quick suggestion buttons
  const quickSuggestions = [
    'Calculate tariff',
    'Run simulation',
    'View history',
    'Help'
  ];

  return (
    <>
      {/* Chatbot Toggle Button */}
      <button
        className={`chatbot-toggle ${isOpen ? 'open' : ''}`}
        onClick={toggleChat}
        aria-label="Toggle chatbot"
      >
        {isOpen ? (
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        ) : (
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
        )}
      </button>

      {/* Chatbot Window */}
      {isOpen && (
        <div className="chatbot-window">
          {/* Header */}
          <div className="chatbot-header">
            <div className="chatbot-header-info">
              <div className="chatbot-avatar">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z" />
                </svg>
              </div>
              <div>
                <h3>TariffKey Assistant</h3>
                <span className="chatbot-status">Online</span>
              </div>
            </div>
            <button
              className="chatbot-clear"
              onClick={handleClearChat}
              aria-label="Clear chat"
              title="Clear chat"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              </svg>
            </button>
          </div>

          {/* Messages */}
          <div className="chatbot-messages">
            {messages.map((message) => (
              <div
                key={message.id}
                className={`message ${message.sender === 'user' ? 'message-user' : 'message-bot'}`}
              >
                <div className="message-content">
                  <div className="message-text">
                    {message.text.split('\n').map((line, index) => (
                      <React.Fragment key={index}>
                        {line}
                        {index < message.text.split('\n').length - 1 && <br />}
                      </React.Fragment>
                    ))}
                  </div>
                  {message.actions && message.actions.length > 0 && (
                    <div className="message-actions">
                      {message.actions.map((action, index) => (
                        <button
                          key={index}
                          className="action-button"
                          onClick={() => handleAction(action)}
                        >
                          {action.label}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
                <span className="message-time">
                  {message.timestamp.toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </span>
              </div>
            ))}

            {/* Typing indicator */}
            {isTyping && (
              <div className="message message-bot">
                <div className="message-content">
                  <div className="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Quick Suggestions */}
          {messages.length <= 2 && (
            <div className="chatbot-suggestions">
              <p>Quick actions:</p>
              <div className="suggestion-buttons">
                {quickSuggestions.map((suggestion, index) => (
                  <button
                    key={index}
                    className="suggestion-button"
                    onClick={() => handleQuickSuggestion(suggestion)}
                  >
                    {suggestion}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Input Form */}
          <form className="chatbot-input" onSubmit={handleSendMessage}>
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              placeholder="Type your message..."
              maxLength={500}
            />
            <button type="submit" disabled={!inputMessage.trim()}>
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
              </svg>
            </button>
          </form>
        </div>
      )}
    </>
  );
};

export default Chatbot;
