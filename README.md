# 📊 **AnalyticsManager SDK for Android**

`AnalyticsManager` is a simple and lightweight analytics SDK designed for Native Android (Kotlin) applications. This SDK enables developers to seamlessly track user sessions, custom events, and screen time. The SDK also ensures that session data persists even when the app is restarted, thanks to `SharedPreferences` integration.

---

---

## 🚀 **Features**

### 1. **Session Management**
- **Start a Session:** Initiate a new analytics session with a unique session ID.  
- **End a Session:** End the current session and persist the session data.

### 2. **Event Tracking**
- **Track Events:** Record custom events with associated properties.  
- **Track Screen Time:** Measure and log the time a user spends on specific screens.

### 3. **Data Persistence**
- **Persistent Storage:** Session and event data are stored in `SharedPreferences` to ensure data consistency across app launches.

### 4. **Thread-Safe Singleton**
- The `AnalyticsManager` is implemented as a thread-safe singleton to ensure only one instance is used throughout the app.
