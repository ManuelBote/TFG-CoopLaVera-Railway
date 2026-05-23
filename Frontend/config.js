// Frontend/config.js
const CONFIG = {
    API_URL: 'https://tfg-cooplavera-back-production.up.railway.app/api', 

    getAuthHeaders() {
        return {
            headers: {
                Authorization: `Bearer ${localStorage.getItem('token')}`
            }
        };
    }
};