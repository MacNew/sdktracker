package org.tracker.trackersdk

import org.junit.Test
import org.junit.Assert.assertEquals
import org.tracker.trackersdk.data.model.AnalyticsEvent
import org.tracker.trackersdk.utils.EventParser.Companion.parseEvents

class EventParserTest {

    @Test
    fun `parseEvents correctly parses valid input data`() {
        val data = "ButtonClicked:screen=MainActivity,action=TrackEvent;ScreenTime:screenName=DetailActivity,durationInSeconds=5"

        val expected = listOf(
            AnalyticsEvent(
                eventName = "ButtonClicked",
                properties = mutableMapOf(
                    "screen" to "MainActivity",
                    "action" to "TrackEvent"
                )
            ),
            AnalyticsEvent(
                eventName = "ScreenTime",
                properties = mutableMapOf(
                    "screenName" to "DetailActivity",
                    "durationInSeconds" to "5"
                )
            )
        )

        val actual = parseEvents(data)

        assertEquals(expected, actual)
    }

    @Test
    fun `parseEvents returns empty list for empty input`() {
        val data = ""

        val expected = emptyList<AnalyticsEvent>()

        val actual = parseEvents(data)

        assertEquals(expected, actual)
    }

    @Test
    fun `parseEvents ignores malformed entries`() {
        val data = "ButtonClicked:screen=MainActivity,action=TrackEvent;MalformedEntry;ScreenTime:screenName=HomeActivity,durationInSeconds=3"

        val expected = listOf(
            AnalyticsEvent(
                eventName = "ButtonClicked",
                properties = mutableMapOf(
                    "screen" to "MainActivity",
                    "action" to "TrackEvent"
                )
            ),
            AnalyticsEvent(
                eventName = "ScreenTime",
                properties = mutableMapOf(
                    "screenName" to "HomeActivity",
                    "durationInSeconds" to "3"
                )
            )
        )

        val actual = parseEvents(data)

        assertEquals(expected, actual)
    }

    @Test
    fun `It should be fail because data is not valid`() {
        val data = "ButtonClicked:jkjkscregjhghjgheenTime:screenName=HomeActivity,durationInSeconds=0"

        val expected = listOf(
            AnalyticsEvent(
                eventName = "ButtonClicked",
                properties = mutableMapOf(
                    "screen" to "MainActivity"
                )
            ),
            AnalyticsEvent(
                eventName = "ScreenTime",
                properties = mutableMapOf(
                    "screenName" to "HomeActivity",
                    "durationInSeconds" to "0"
                )
            )
        )

        val actual = parseEvents(data)

        assertEquals(expected, actual)
    }
}