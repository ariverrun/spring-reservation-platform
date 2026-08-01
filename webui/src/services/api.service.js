import TokenManager from '../utils/tokenManager';

const API_BASE_URL = '/api/v1';

class ApiService {
  static async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    const token = TokenManager.getAccessToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      if (response.status === 401) {
        const refreshToken = TokenManager.getRefreshToken();
        if (refreshToken) {
          const refreshed = await this.refreshToken();
          if (refreshed) {
            const newToken = TokenManager.getAccessToken();
            headers['Authorization'] = `Bearer ${newToken}`;
            const retryResponse = await fetch(url, {
              ...options,
              headers,
            });
            return this.handleResponse(retryResponse);
          }
        }
        
        TokenManager.clearTokens();
        window.location.href = '/login';
        throw new Error('Session expired');
      }

      return this.handleResponse(response);
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  static async handleResponse(response) {
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw {
        status: response.status,
        message: error.message || error.error || 'Request failed',
        data: error,
      };
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return response.json();
    }
    return response.text();
  }

  static async refreshToken() {
    try {
      const refreshToken = TokenManager.getRefreshToken();
      if (!refreshToken) {
        return false;
      }

      const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken }),
      });

      if (response.ok) {
        const data = await response.json();
        if (data.accessToken && data.refreshToken) {
          const userData = TokenManager.getUser();
          TokenManager.setTokens(
            data.accessToken,
            data.refreshToken,
            userData
          );
          return true;
        }
      }
      return false;
    } catch (error) {
      console.error('Token refresh failed:', error);
      return false;
    }
  }

  static get(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'GET' });
  }

  static post(endpoint, data, options = {}) {
    return this.request(endpoint, {
      ...options,
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  static put(endpoint, data, options = {}) {
    return this.request(endpoint, {
      ...options,
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  static delete(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'DELETE' });
  }
}

export default ApiService;