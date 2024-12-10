package org.tracker.trackersdk
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.tracker.trackersdk.model.AnalyticsEvent
import java.util.UUID

class AnalyticsManager private constructor(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "AnalyticsPrefs"
        private const val KEY_SESSION_ID = "SessionId"
        private const val KEY_EVENTS = "Events"

        @Volatile
        private var INSTANCE: AnalyticsManager? = null

        fun getInstance(context: Context): AnalyticsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AnalyticsManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private var currentSessionId: String? = null
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val events: MutableList<AnalyticsEvent> = mutableListOf()

    /**
     * Starts a new analytics session.
     */
    fun startSession() {
        currentSessionId = UUID.randomUUID().toString()
        events.clear()
        sharedPreferences.edit().putString(KEY_SESSION_ID, currentSessionId).apply()
        Log.d("AnalyticsManager", "Session started: $currentSessionId")
    }

    /**
     * Ends the current analytics session and persists data.
     */
    fun endSession() {
        if (currentSessionId != null) {
            persistSessionData()
            Log.d("AnalyticsManager", "Session ended: $currentSessionId")
        } else {
            Log.e("AnalyticsManager", "No active session to end")
        }
        currentSessionId = null
    }

    /**
     * Logs an event in the current session.
     */
    fun trackEvent(event: AnalyticsEvent) {
        if (currentSessionId == null) {
            Log.e("AnalyticsManager", "No active session. Start a session first.")
            return
        }
        events.add(event)
        Log.d("AnalyticsManager", "Event tracked: ${event.eventName} with properties: ${event.properties}")
    }

    /**
     * Persists session data.
     */
    private fun persistSessionData() {
        val eventsJson = events.joinToString(separator = ";") { event ->
            "${event.eventName}:${event.properties.entries.joinToString(",") { "${it.key}=${it.value}" }}"
        }
        sharedPreferences.edit().putString(KEY_EVENTS, eventsJson).apply()
    }

    /**
     * Retrieves persisted session data.
     */
    fun getPersistedSessionData(): String {
        return sharedPreferences.getString(KEY_EVENTS, "") ?: ""
    }
}
