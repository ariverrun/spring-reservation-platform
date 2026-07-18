import ApiService from './api.service';
import TokenManager from '../utils/tokenManager';

class AuthService {
  static async login(email, password) {
    try {
      const response = await ApiService.post('/auth/login', { email, password });
      if (response.accessToken && response.refreshToken) {
        // Получаем информацию о пользователе для определения роли
        // Предполагаем, что в ответе приходит user с role
        const userData = response.user || { role: 'USER' };
        TokenManager.setTokens(
          response.accessToken, 
          response.refreshToken,
          userData
        );
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
    window.location.href = '/';
  }

  static isAuthenticated() {
    return TokenManager.isAuthenticated();
  }

  static isAdmin() {
    return TokenManager.isAdmin();
  }

  static getUser() {
    return TokenManager.getUser();
  }
}

export default AuthService;