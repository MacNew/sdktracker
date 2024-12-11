package org.tracker.trackersdk.utils

import org.tracker.trackersdk.data.model.AnalyticsEvent


internal class EventParser {
    companion object {
        @JvmStatic
        fun parseEvents(data: String): List<AnalyticsEvent> {
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
    }
}