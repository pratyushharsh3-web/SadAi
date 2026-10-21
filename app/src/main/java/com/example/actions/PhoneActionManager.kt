package com.example.actions

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhoneActionManager(private val context: Context) {

    // --- APP LAUNCHING ---

    fun openAppByIntent(packageName: String, appName: String): ActionResult {
        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ActionResult.Success(
                    spokenFeedback = "Ji, main $appName khol rahi hoon.",
                    details = "Launched $packageName",
                    actionType = "APP_OPEN"
                )
            } else {
                ActionResult.Failure(
                    spokenFeedback = "Hmm... Lagta hai $appName aapke phone me installed nahi hai ji.",
                    errorDetail = "Package $packageName not found"
                )
            }
        } catch (e: Exception) {
            Log.e("PhoneAction", "Error launching $packageName", e)
            ActionResult.Failure(
                spokenFeedback = "Sorry ji, $appName open karne me thodi problem aayi.",
                errorDetail = e.message
            )
        }
    }

    fun openYouTube(): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("Ji bilkul! Main YouTube khol rahi hoon.", "Opened YouTube", "APP_OPEN")
        } catch (e: Exception) {
            openAppByIntent("com.google.android.youtube", "YouTube")
        }
    }

    fun openCamera(): ActionResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("Theek hai ji, camera open kar rahi hoon.", "Opened Camera", "APP_OPEN")
        } catch (e: Exception) {
            ActionResult.Failure("Camera open nahi ho paya ji.", e.message)
        }
    }

    fun openCalculator(): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("Achha ji, calculator open kar diya hai.", "Opened Calculator", "APP_OPEN")
        } catch (e: Exception) {
            val knownCalcPackages = listOf(
                "com.google.android.calculator",
                "com.sec.android.app.popupcalculator",
                "com.miui.calculator",
                "com.coloros.calculator"
            )
            for (pkg in knownCalcPackages) {
                val res = openAppByIntent(pkg, "Calculator")
                if (res is ActionResult.Success) return res
            }
            ActionResult.Failure("Calculator app nahi mila ji.")
        }
    }

    fun openSettings(): ActionResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("Ji, Android Settings khol rahi hoon.", "Opened Settings", "SETTINGS")
        } catch (e: Exception) {
            ActionResult.Failure("Settings open nahi ho payi ji.", e.message)
        }
    }

    fun openWifiSettings(): ActionResult {
        return openSystemSetting(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi settings")
    }

    fun openBluetoothSettings(): ActionResult {
        return openSystemSetting(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth settings")
    }

    fun openSoundSettings(): ActionResult {
        return openSystemSetting(Settings.ACTION_SOUND_SETTINGS, "Sound settings")
    }

    fun openDisplaySettings(): ActionResult {
        return openSystemSetting(Settings.ACTION_DISPLAY_SETTINGS, "Display settings")
    }

    fun openBatterySettings(): ActionResult {
        return openSystemSetting(Settings.ACTION_BATTERY_SAVER_SETTINGS, "Battery settings")
    }

    fun openLocationSettings(): ActionResult {
        return openSystemSetting(Settings.ACTION_LOCATION_SOURCE_SETTINGS, "Location settings")
    }

    private fun openSystemSetting(action: String, title: String): ActionResult {
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("Ji, $title open kar diya hai.", "Opened $action", "SETTINGS")
        } catch (e: Exception) {
            openSettings()
        }
    }

    // --- BATTERY & SYSTEM INFO ---

    fun getBatteryInfo(): BatteryInfo {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 0

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val statusText = if (isCharging) "charging par hai ji" else "charging par nahi hai"

        return BatteryInfo(batteryPct, isCharging, statusText)
    }

    fun getBatteryReportSpoken(): ActionResult {
        val info = getBatteryInfo()
        val speech = "Ji, aapke phone ki battery ${info.percentage} percent hai aur phone abhi ${info.statusText}."
        return ActionResult.Success(speech, "Battery: ${info.percentage}%, Charging: ${info.isCharging}", "BATTERY")
    }

    fun getCurrentTimeReport(): ActionResult {
        val formatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val timeHindi = SimpleDateFormat("h bajkar mm minute", Locale.getDefault()).format(Date())
        return ActionResult.Success("Ji, abhi samay $timeHindi hua hai ($formatted).", "Time: $formatted", "TIME")
    }

    fun getCurrentDateReport(): ActionResult {
        val englishDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).format(Date())
        return ActionResult.Success("Ji, aaj ka din $englishDate hai.", "Date: $englishDate", "DATE")
    }

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // --- AUDIO & VOLUME ---

    fun adjustVolume(direction: Int): ActionResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val flag = AudioManager.FLAG_SHOW_UI
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, flag)
            val desc = if (direction > 0) "Ji, volume badha diya hai." else "Theek hai, volume kam kar diya hai ji."
            ActionResult.Success(desc, "Volume adjusted", "VOLUME")
        } catch (e: Exception) {
            ActionResult.Failure("Volume adjust nahi ho paya ji.", e.message)
        }
    }

    // --- CONTACTS & CALLING ---

    fun searchContacts(query: String): List<ContactInfo> {
        val list = mutableListOf<ContactInfo>()
        if (context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return list
        }

        try {
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            )
            val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%$query%")

            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex) ?: ""
                    val number = cursor.getString(numberIndex) ?: ""
                    val photo = if (photoIndex != -1) cursor.getString(photoIndex) else null
                    if (name.isNotEmpty() && number.isNotEmpty()) {
                        list.add(ContactInfo(name, number, photo))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PhoneAction", "Error searching contacts", e)
        }
        return list.distinctBy { it.phoneNumber }
    }

    fun openDialer(phoneNumber: String, contactName: String? = null): ActionResult {
        return try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val target = contactName ?: cleanNumber
            ActionResult.Success("Ji, $target ke liye dialer open kar diya hai.", "Dialing $cleanNumber", "CALL")
        } catch (e: Exception) {
            ActionResult.Failure("Dialer open nahi ho saka ji.", e.message)
        }
    }

    // --- SMS DRAFT ---

    fun prepareSms(phoneNumber: String, messageBody: String, contactName: String? = null): ActionResult {
        return try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanNumber")
                putExtra("sms_body", messageBody)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val target = contactName ?: cleanNumber
            ActionResult.Success(
                spokenFeedback = "Achha ji, $target ke liye message draft taiyaar kar diya hai.",
                details = "SMS to $cleanNumber: $messageBody",
                actionType = "SMS"
            )
        } catch (e: Exception) {
            ActionResult.Failure("SMS app open karne me problem aayi ji.", e.message)
        }
    }

    // --- MAPS & NAVIGATION ---

    fun openMaps(destination: String): ActionResult {
        return try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(destination)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(destination)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            }
            ActionResult.Success("Theek hai ji, $destination ke liye Maps open kar rahi hoon.", "Maps search: $destination", "MAPS")
        } catch (e: Exception) {
            ActionResult.Failure("Maps open karne me dikkat aayi ji.", e.message)
        }
    }
}
