package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.intent.ParsedIntent
import com.example.intent.SadbhavnaIntentEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sadbhavna AI", appName)
    }

    @Test
    fun `test offline intent parsing for youtube and camera`() {
        val engine = SadbhavnaIntentEngine()
        val res1 = engine.parse("Jarvis, YouTube kholo")
        assertTrue(res1 is ParsedIntent.OpenYouTube)

        val res2 = engine.parse("Camera open karo")
        assertTrue(res2 is ParsedIntent.OpenCamera)

        val res3 = engine.parse("Hey Jarvis, settings kholo")
        assertTrue(res3 is ParsedIntent.OpenSettings)
    }

    @Test
    fun `test offline intent parsing for battery and time`() {
        val engine = SadbhavnaIntentEngine()
        val res1 = engine.parse("Meri battery kitni hai?")
        assertTrue(res1 is ParsedIntent.GetBattery)

        val res2 = engine.parse("Kitne baje hain?")
        assertTrue(res2 is ParsedIntent.GetTime)
    }

    @Test
    fun `test calling intent parsing`() {
        val engine = SadbhavnaIntentEngine()
        val res = engine.parse("Papa ko call karo")
        assertTrue(res is ParsedIntent.CallContact)
        assertEquals("papa", (res as ParsedIntent.CallContact).contactName.lowercase())
    }
}
