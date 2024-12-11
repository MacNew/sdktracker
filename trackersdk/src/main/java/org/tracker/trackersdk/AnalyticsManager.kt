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
        private const val MyLog = "AnalyticsManager"

        @Volatile
        private var INSTANCE: AnalyticsManager? = null

        fun getInstance(context: Context): AnalyticsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AnalyticsManager(context.applicationContext)
                instance.loadPersistedSession()
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

    init {
        loadPersistedSession()
    }

    // Load persisted session and events on initialization
    private fun loadPersistedSession() {
        currentSessionId = sharedPreferences.getString(KEY_SESSION_ID, null)
        val eventsData = sharedPreferences.getString(KEY_EVENTS, "")
        if (!eventsData.isNullOrEmpty()) {
            events.clear()
            events.addAll(parseEvents(eventsData))
        }
    }

    fun startSession(sessionId: String, result: (Result<String>) -> Unit) {
        currentSessionId = sessionId
        events.clear()
        persistSessionData()
        result.invoke(Result.Success(sessionId))
        Log.d(MyLog, "Session started: $currentSessionId")
    }

    fun endSession(result: (Result<String>) -> Unit) {
        if (currentSessionId != null) {
            persistSessionData()
            result.invoke(Result.Success("Session ended: $currentSessionId"))
            Log.d(MyLog, "Session ended: $currentSessionId")
        } else {
            result.invoke(Result.Error("No active session to end"))
            Log.e(MyLog, "No active session to end")
        }
        currentSessionId = null
        sharedPreferences.edit().remove(KEY_SESSION_ID).apply()
    }

    fun trackEvent(event: AnalyticsEvent, result: (Result<String>) -> Unit) {
        if (currentSessionId == null) {
            result.invoke(Result.Error("No active session. Start a session first."))
            Log.e(MyLog, "No active session. Start a session first.")
            return
        }
        events.add(event)
        persistSessionData()
        result.invoke(Result.Success("Event tracked: ${event.eventName} with properties: ${event.properties}"))
        Log.d(MyLog, "Event tracked: ${event.eventName} with properties: ${event.properties}")
    }

    fun startScreenTracking(screenName: String, result: (Result<String>) -> Unit) {
        if (currentSessionId == null) {
            result.invoke(Result.Error("No active session. Start a session first."))
            Log.e("AnalyticsManager", "No active session. Start a session first.")
            return
        }
        screenStartTimes[screenName] = SystemClock.elapsedRealtime()
        result.invoke(Result.Success("Screen Time Recorded Successfully for $screenName"))
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
            Log.e(MyLog, "Screen tracking not started for $screenName")
        }
    }

    private fun persistSessionData() {
        sharedPreferences.edit().apply {
            putString(KEY_SESSION_ID, currentSessionId)
            val eventsJson = events.joinToString(separator = ";") { event ->
                "${event.eventName}:${event.properties.entries.joinToString(",") { "${it.key}=${it.value}" }}"
            }
            putString(KEY_EVENTS, eventsJson)
            apply()
        }
    }

    private fun parseEvents(data: String): List<AnalyticsEvent> {
        return data.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val eventName = parts[0]
                val properties = parts[1].split(",").associate {
                    val keyValue = it.split("=")
                    (if (keyValue.size == 2) keyValue[0] to keyValue[1] else null)!!
                }
                AnalyticsEvent(eventName, properties.toMutableMap())
            } else null
        }
    }

    fun getPersistedSessionData(): String {
        return sharedPreferences.getString(KEY_EVENTS, "") ?: ""
    }

    fun getCurrentSession(): String? {
        return currentSessionId
    }

    fun getEvents(): List<AnalyticsEvent> {
        return events.toList()
    }
}