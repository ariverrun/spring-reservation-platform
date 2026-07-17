import ApiService from './api.service';
import TokenManager from '../utils/tokenManager';

class AuthService {
  static async login(email, password) {
    try {
      const response = await ApiService.post('/auth/login', { email, password });
      if (response.accessToken && response.refreshToken) {
        TokenManager.setTokens(response.accessToken, response.refreshToken);
        return { success: true, data: response };
      }
      return { success: false, error: 'Invalid response from server' };
    } catch (error) {
      return { success: false, error: error.message };
    }
  }

  static async register(email, firstName, lastName, password) {
    try {
      const response = await ApiService.post('/user', {
        email,
        firstName,
        lastName,
        password,
      });
      return { success: true, data: response };
    } catch (error) {
      return { success: false, error: error.message };
    }
  }

  static logout() {
    TokenManager.clearTokens();
    window.location.href = '/login';
  }

  static isAuthenticated() {
    return TokenManager.isAuthenticated();
  }
}

export default AuthService;