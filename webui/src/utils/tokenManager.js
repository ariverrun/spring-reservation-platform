const TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_KEY = 'user_data';
const TOKEN_EXPIRY_KEY = 'token_expiry';

class TokenManager {
  static listeners = [];

  static setTokens(accessToken, refreshToken, userData = null) {
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    
    if (userData) {
      localStorage.setItem(USER_KEY, JSON.stringify(userData));
    }

    try {
      const payload = JSON.parse(atob(accessToken.split('.')[1]));
      if (payload.exp) {
        localStorage.setItem(TOKEN_EXPIRY_KEY, String(payload.exp * 1000));
      }
    } catch (e) {
      console.debug('Could not parse token expiry');
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

  static getTokenExpiry() {
    const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY);
    return expiry ? parseInt(expiry) : null;
  }

  static isTokenExpired() {
    const expiry = this.getTokenExpiry();
    if (!expiry) return false;
    return Date.now() > expiry;
  }

  static clearTokens() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(TOKEN_EXPIRY_KEY);
    this.notifyListeners();
  }

  static isAuthenticated() {
    const token = this.getAccessToken();
    if (!token) return false;
    if (this.isTokenExpired()) {
      this.clearTokens();
      return false;
    }
    return true;
  }

  static isAdmin() {
    const user = this.getUser();
    if (!user || !user.roles) return false;
    return user.roles.some(role => 
      role === 'ROLE_ADMIN' || role === 'ADMIN'
    );
  }

  static getUserId() {
    const user = this.getUser();
    return user?.userId || null;
  }

  static getEmail() {
    const user = this.getUser();
    return user?.email || null;
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