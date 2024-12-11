package org.tracker.trackersdk

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Log
import org.tracker.trackersdk.data.Result
import org.tracker.trackersdk.data.model.AnalyticsEvent

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

    private val screenStartTimes: MutableMap<String, Long> = mutableMapOf()

    fun startSession(sessionId: String, result: (Result<String>) -> Unit) {
        currentSessionId = sessionId
        events.clear()
        sharedPreferences.edit().putString(KEY_SESSION_ID, currentSessionId).apply()
        result.invoke(Result.Success(sessionId))
        Log.d("AnalyticsManager", "Session started: $currentSessionId")
    }

    fun endSession(result: (Result<String>) -> Unit) {
        if (currentSessionId != null) {
            persistSessionData()
            result.invoke(Result.Success("Session ended: $currentSessionId"))
            Log.d("AnalyticsManager", "Session ended: $currentSessionId")
        } else {
            result.invoke(Result.Error("No active session to end"))
            Log.e("AnalyticsManager", "No active session to end")
        }
        currentSessionId = null
    }

    fun trackEvent(event: AnalyticsEvent, result: (Result<String>) -> Unit) {
        if (currentSessionId == null) {
            result.invoke(Result.Error("No active session. Start a session first."))
            Log.e("AnalyticsManager", "No active session. Start a session first.")
            return
        }
        events.add(event)
        result.invoke(Result.Success("Event tracked: ${event.eventName} with properties: ${event.properties}"))
        Log.d("AnalyticsManager", "Event tracked: ${event.eventName} with properties: ${event.properties}")
    }

    fun startScreenTracking(screenName: String, result: (Result<String>) -> Unit) {
        if (currentSessionId == null) {
            result.invoke(Result.Error("No active session. Start a session first."))
            Log.e("AnalyticsManager", "No active session. Start a session first.")
            return
        }
        screenStartTimes[screenName] = SystemClock.elapsedRealtime()
        result.invoke(Result.Success("Screen Time  Recorded Successfully for $screenName"))
        Log.d("AnalyticsManager", "Screen entered: $screenName")
    }

    fun endScreenTracking(screenName: String, result: (Result<String>) -> Unit) {
        val startTime = screenStartTimes[screenName]
        if (startTime != null) {
            val duration = SystemClock.elapsedRealtime() - startTime
            val event = AnalyticsEvent(
                eventName = "ScreenTime",
                properties = mutableMapOf(
                    "screenName" to screenName,
                    "durationInSeconds" to (duration / 1000).toString()
                )
            )
            trackEvent(event, result)
            screenStartTimes.remove(screenName)
        } else {
            result(Result.Error("Screen tracking not started for $screenName"))
            Log.e("AnalyticsManager", "Screen tracking not started for $screenName")
        }
    }

    private fun persistSessionData() {
        val eventsJson = events.joinToString(separator = ";") { event ->
            "${event.eventName}:${event.properties.entries.joinToString(",") { "${it.key}=${it.value}" }}"
        }
        sharedPreferences.edit().putString(KEY_EVENTS, eventsJson).apply()
    }

    fun getPersistedSessionData(): String {
        return sharedPreferences.getString(KEY_EVENTS, "") ?: ""
    }

    fun  getCurrentSession(): String? {
        return currentSessionId;
    }


    fun getEvents(): List<AnalyticsEvent> {
        return events.toList()
    }
}