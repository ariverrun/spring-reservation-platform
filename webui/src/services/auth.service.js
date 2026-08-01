import ApiService from './api.service';
import TokenManager from '../utils/tokenManager';

class AuthService {
  static async login(email, password) {
    try {
      const response = await ApiService.post('/auth/login', { email, password });
      
      if (response.accessToken && response.refreshToken) {
        const userData = {
          userId: response.userId,
          email: response.email,
          roles: response.roles || []
        };
        
        TokenManager.setTokens(
          response.accessToken,
          response.refreshToken,
          userData
        );
        
        return { 
          success: true, 
          data: response,
          isAdmin: userData.roles.some(r => r === 'ROLE_ADMIN')
        };
      }
      
      return { 
        success: false, 
        error: 'Invalid response from server' 
      };
    } catch (error) {
      console.error('Login error:', error);
      return { 
        success: false, 
        error: error.message || 'Login failed' 
      };
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
      console.error('Registration error:', error);
      return { 
        success: false, 
        error: error.message || 'Registration failed' 
      };
    }
  }

  static logout() {
    TokenManager.clearTokens();
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

  static getUserId() {
    return TokenManager.getUserId();
  }
}

export default AuthService;