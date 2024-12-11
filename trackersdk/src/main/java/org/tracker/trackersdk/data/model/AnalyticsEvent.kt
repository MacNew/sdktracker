package org.tracker.trackersdk.data.model

data class AnalyticsEvent(
    val eventName: String,
    val properties: MutableMap<String, String> = mutableMapOf()
)
