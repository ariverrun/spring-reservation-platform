const TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_KEY = 'user_data';
const TOKEN_EXPIRY_KEY = 'token_expiry';

class TokenManager {
  static listeners = [];
  static refreshTimer = null;

  static setTokens(accessToken, refreshToken, userData = null) {
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    
    if (userData) {
      localStorage.setItem(USER_KEY, JSON.stringify(userData));
    }

    try {
      const payload = JSON.parse(atob(accessToken.split('.')[1]));
      if (payload.exp) {
        const expiryMs = payload.exp * 1000;
        localStorage.setItem(TOKEN_EXPIRY_KEY, String(expiryMs));
        this.scheduleRefresh(expiryMs);
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

  static getTimeUntilExpiry() {
    const expiry = this.getTokenExpiry();
    if (!expiry) return 0;
    return expiry - Date.now();
  }

  static scheduleRefresh(expiryMs) {
    this.clearRefreshTimer();

    const now = Date.now();
    const timeUntilExpiry = expiryMs - now;
    
    if (timeUntilExpiry < 60000) {
      console.warn('Token already expired or expires in less than 1 minute');
      this.notifyListeners();
      return;
    }

    const refreshDelay = timeUntilExpiry - 60000;
    
    console.log(`Token refresh scheduled in ${Math.round(refreshDelay / 1000)} seconds`);
    
    this.refreshTimer = setTimeout(async () => {
      console.log('⏰ Auto-refreshing token...');
      try {
        const refreshToken = this.getRefreshToken();
        if (refreshToken) {
          const response = await fetch('/api/v1/auth/refresh', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ refreshToken }),
          });

          if (response.ok) {
            const data = await response.json();
            if (data.accessToken && data.refreshToken) {
              const userData = this.getUser();
              this.setTokens(data.accessToken, data.refreshToken, userData);
              console.log('✅ Token refreshed successfully');
              this.notifyListeners();
            }
          } else {
            console.error('❌ Token refresh failed');
            this.clearTokens();
            this.notifyListeners();
          }
        }
      } catch (error) {
        console.error('❌ Token refresh error:', error);
        this.clearTokens();
        this.notifyListeners();
      }
    }, refreshDelay);
  }

  static clearRefreshTimer() {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }
  }

  static clearTokens() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(TOKEN_EXPIRY_KEY);
    this.clearRefreshTimer();
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