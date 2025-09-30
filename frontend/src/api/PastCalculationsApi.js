import { 
    API_BASE_URL, 
    API_ENDPOINTS, 
    DEFAULTS, 
    getHeaders 
} from './config';

export async function savePastCalculation(tariffResponse) {
    const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.SAVE_CALCULATION}`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(tariffResponse)
    });
    if (!response.ok) {
        throw new Error('Failed to save calculation');
    }
    return response.json();
}

export async function getPastCalculations() {
    const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.GET_HISTORY}`, {
        headers: getHeaders()
    });
    if (!response.ok) {
        throw new Error('Failed to fetch calculation history');
    }
    return response.json();
}

export async function getRecentCalculations(days = DEFAULTS.RECENT_DAYS) {
    const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.GET_RECENT}?days=${days}`, {
        headers: getHeaders()
    });
    if (!response.ok) {
        throw new Error('Failed to fetch recent calculations');
    }
    return response.json();
}