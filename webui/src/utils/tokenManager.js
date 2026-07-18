const TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_KEY = 'user_data';

class TokenManager {
  static listeners = [];

  static setTokens(accessToken, refreshToken, userData = null) {
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    if (userData) {
      localStorage.setItem(USER_KEY, JSON.stringify(userData));
    }
    this.notifyListeners();
  }

  static getAccessToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  static getRefreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  static getUser() {
    const userData = localStorage.getItem(USER_KEY);
    try {
      return userData ? JSON.parse(userData) : null;
    } catch {
      return null;
    }
  }

  static clearTokens() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.notifyListeners();
  }

  static isAuthenticated() {
    return !!this.getAccessToken();
  }

  static isAdmin() {
    const user = this.getUser();
    return user && user.role === 'ADMIN';
  }

  static addListener(callback) {
    this.listeners.push(callback);
  }

  static removeListener(callback) {
    this.listeners = this.listeners.filter(cb => cb !== callback);
  }

  static notifyListeners() {
    this.listeners.forEach(callback => callback(this.isAuthenticated()));
  }
}

export default TokenManager;