package com.example.intent

import java.util.Locale

sealed class ParsedIntent {
    object OpenYouTube : ParsedIntent()
    object OpenCamera : ParsedIntent()
    object OpenCalculator : ParsedIntent()
    object OpenSettings : ParsedIntent()
    data class OpenSettingSpecific(val type: String) : ParsedIntent()
    object GetBattery : ParsedIntent()
    object GetTime : ParsedIntent()
    object GetDate : ParsedIntent()
    data class CallContact(val contactName: String) : ParsedIntent()
    data class SendSms(val contactName: String, val messageText: String) : ParsedIntent()
    data class CreateReminder(val title: String, val category: String, val hourOfDay: Int?, val minute: Int?) : ParsedIntent()
    object ReadTasks : ParsedIntent()
    data class AdjustVolume(val isUp: Boolean) : ParsedIntent()
    object StopSpeaking : ParsedIntent()
    data class ToggleAiMode(val mode: String) : ParsedIntent()
    data class OpenMaps(val query: String) : ParsedIntent()
    data class GeneralAiQuery(val query: String) : ParsedIntent()
}

class SadbhavnaIntentEngine {

    fun parse(input: String): ParsedIntent {
        val cleaned = input.trim()
            .lowercase(Locale.ROOT)
            .replace("jarvis", "")
            .replace("sadbhavna", "")
            .replace("hey", "")
            .replace("suno", "")
            .replace("sunno", "")
            .replace("kripya", "")
            .replace("please", "")
            .trim()

        // 1. Stop speaking / silence
        if (cleaned.contains("stop speaking") || cleaned.contains("chup") || cleaned.contains("shant ho jao") || cleaned.contains("ruk jao")) {
            return ParsedIntent.StopSpeaking
        }

        // 2. Mode switches
        if (cleaned.contains("offline mode") || cleaned.contains("offline on")) {
            return ParsedIntent.ToggleAiMode("offline")
        }
        if (cleaned.contains("online mode") || cleaned.contains("hybrid mode")) {
            return ParsedIntent.ToggleAiMode("hybrid")
        }

        // 3. System time & date
        if (cleaned.contains("time") || cleaned.contains("kitne baje") || cleaned.contains("samay")) {
            return ParsedIntent.GetTime
        }
        if (cleaned.contains("date") || cleaned.contains("tarikh") || cleaned.contains("aaj ka din")) {
            return ParsedIntent.GetDate
        }

        // 4. Battery
        if (cleaned.contains("battery") || cleaned.contains("charging")) {
            return ParsedIntent.GetBattery
        }

        // 5. Volume
        if (cleaned.contains("volume badhao") || cleaned.contains("volume up") || cleaned.contains("awaz badhao")) {
            return ParsedIntent.AdjustVolume(isUp = true)
        }
        if (cleaned.contains("volume kam") || cleaned.contains("volume down") || cleaned.contains("awaz kam")) {
            return ParsedIntent.AdjustVolume(isUp = false)
        }

        // 6. Application launching
        if (cleaned.contains("youtube")) {
            return ParsedIntent.OpenYouTube
        }
        if (cleaned.contains("camera") || cleaned.contains("photo khicho") || cleaned.contains("selfie")) {
            return ParsedIntent.OpenCamera
        }
        if (cleaned.contains("calculator") || cleaned.contains("hisab")) {
            return ParsedIntent.OpenCalculator
        }

        // 7. Settings
        if (cleaned.contains("wifi") || cleaned.contains("wi-fi")) {
            return ParsedIntent.OpenSettingSpecific("wifi")
        }
        if (cleaned.contains("bluetooth")) {
            return ParsedIntent.OpenSettingSpecific("bluetooth")
        }
        if (cleaned.contains("display") || cleaned.contains("brightness") || cleaned.contains("roshni")) {
            return ParsedIntent.OpenSettingSpecific("display")
        }
        if (cleaned.contains("sound") || cleaned.contains("ringtone") || cleaned.contains("dhwani")) {
            return ParsedIntent.OpenSettingSpecific("sound")
        }
        if (cleaned.contains("location") || cleaned.contains("gps")) {
            return ParsedIntent.OpenSettingSpecific("location")
        }
        if (cleaned.contains("settings") || cleaned.contains("setting")) {
            return ParsedIntent.OpenSettings
        }

        // 8. Maps & Navigation
        if (cleaned.contains("maps kholo") || cleaned.contains("map open") || cleaned.startsWith("raasta") || cleaned.contains("navigate to")) {
            val dest = cleaned.replace("maps kholo", "").replace("map open", "").replace("navigate to", "").trim()
            val target = if (dest.isNotEmpty()) dest else "Current Location"
            return ParsedIntent.OpenMaps(target)
        }

        // 9. Calling ("Papa ko call karo", "Call Rahul", "Mummy ko call lagao")
        val callRegex1 = Regex("""(?:call|phone)\s+([a-zA-Z0-9\s]+)""", RegexOption.IGNORE_CASE)
        val callRegex2 = Regex("""([a-zA-Z0-9\s]+)\s+(?:ko|par)\s+(?:call|phone)\s+(?:karo|lagao)""", RegexOption.IGNORE_CASE)
        val callMatch2 = callRegex2.find(cleaned)
        if (callMatch2 != null) {
            val name = callMatch2.groupValues[1].trim()
            return ParsedIntent.CallContact(name)
        }
        val callMatch1 = callRegex1.find(cleaned)
        if (callMatch1 != null && !cleaned.contains("settings")) {
            val name = callMatch1.groupValues[1].replace("karo", "").replace("lagao", "").trim()
            if (name.isNotEmpty()) {
                return ParsedIntent.CallContact(name)
            }
        }

        // 10. SMS / Messaging ("Rahul ko message likho ki main 10 minute mein aa raha hoon")
        val smsRegex = Regex("""([a-zA-Z0-9\s]+)\s+(?:ko|par)\s+(?:message|msg|sms)\s+(?:bhejo|likho|karo)(?:\s+ki|\s+that)?\s*(.*)""", RegexOption.IGNORE_CASE)
        val smsMatch = smsRegex.find(cleaned)
        if (smsMatch != null) {
            val name = smsMatch.groupValues[1].trim()
            var body = smsMatch.groupValues[2].trim()
            if (body.isEmpty()) {
                body = "Namaste, Sadbhavna AI dwara sandesh."
            }
            return ParsedIntent.SendSms(name, body)
        }

        // 11. Tasks & Reminders
        if (cleaned.contains("mere tasks") || cleaned.contains("schedule kya hai") || cleaned.contains("aaj ka schedule") || cleaned.contains("list my tasks")) {
            return ParsedIntent.ReadTasks
        }
        if (cleaned.contains("reminder") || cleaned.contains("yaad dilana") || cleaned.contains("task banao") || cleaned.contains("alert lagao")) {
            return parseReminder(input)
        }

        // 12. Default to General AI Query (handled by local knowledge base or Gemini)
        return ParsedIntent.GeneralAiQuery(input)
    }

    private fun parseReminder(rawInput: String): ParsedIntent {
        var hour: Int? = null
        var minute: Int? = null

        // Detect time like "6 baje", "5:30 baje", "10 am", "8 pm"
        val timeRegex = Regex("""(\d{1,2})(?::(\d{2}))?\s*(baje|am|pm)?""", RegexOption.IGNORE_CASE)
        val match = timeRegex.find(rawInput)
        if (match != null) {
            val h = match.groupValues[1].toIntOrNull()
            val m = match.groupValues[2].toIntOrNull() ?: 0
            val modifier = match.groupValues[3].lowercase()

            if (h != null) {
                var calculatedHour = h
                if (modifier == "pm" && calculatedHour < 12) calculatedHour += 12
                if (modifier == "am" && calculatedHour == 12) calculatedHour = 0
                if (modifier == "baje" && (rawInput.contains("sham") || rawInput.contains("raat")) && calculatedHour < 12) {
                    calculatedHour += 12
                }
                hour = calculatedHour
                minute = m
            }
        }

        var category = "General"
        if (rawInput.contains("padhai", ignoreCase = true) || rawInput.contains("study", ignoreCase = true) || rawInput.contains("homework", ignoreCase = true)) {
            category = "Study"
        } else if (rawInput.contains("call", ignoreCase = true)) {
            category = "Call"
        } else if (rawInput.contains("utho", ignoreCase = true) || rawInput.contains("dawa", ignoreCase = true)) {
            category = "Routine"
        }

        val cleanedTitle = rawInput
            .replace(Regex("""(?i)\b(sadbhavna|mujhe|yaad dilana|reminder lagao|reminder|baje|subah|sham|kal)\b"""), "")
            .trim()
            .ifEmpty { "Sadbhavna Reminder" }

        return ParsedIntent.CreateReminder(cleanedTitle, category, hour, minute)
    }
}
