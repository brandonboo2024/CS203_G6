// Base URL for API endpoints
export const API_BASE_URL = 'http://localhost:8080';

// API endpoints for Past Calculations
export const API_ENDPOINTS = {
    SAVE_CALCULATION: '/api/calculations/save',
    GET_HISTORY: '/api/calculations/history',
    GET_RECENT: '/api/calculations/recent'
};

// Default values
export const DEFAULTS = {
    RECENT_DAYS: 30
};

// Headers configuration
export const getHeaders = () => ({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('token')}`
});