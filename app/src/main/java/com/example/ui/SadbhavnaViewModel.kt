package com.example.ui

import android.app.Application
import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.actions.ActionResult
import com.example.actions.BatteryInfo
import com.example.actions.ContactInfo
import com.example.actions.PhoneActionManager
import com.example.ai.GeminiClient
import com.example.data.ChatMessage
import com.example.data.SadbhavnaDatabase
import com.example.data.SadbhavnaRepository
import com.example.data.TaskItem
import com.example.intent.ParsedIntent
import com.example.intent.SadbhavnaIntentEngine
import com.example.reminders.ReminderScheduler
import com.example.speech.AssistantState
import com.example.speech.SpeechRecognitionHelper
import com.example.speech.TextToSpeechHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppTab {
    DASHBOARD,
    CHAT,
    SCHEDULE,
    TOOLS,
    SETTINGS
}

data class ConfirmationData(
    val title: String,
    val description: String,
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit = {}
)

data class ContactSelectionData(
    val title: String,
    val contacts: List<ContactInfo>,
    val onSelected: (ContactInfo) -> Unit,
    val onCancel: () -> Unit = {}
)

class SadbhavnaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SadbhavnaRepository
    private val phoneManager: PhoneActionManager
    private val intentEngine: SadbhavnaIntentEngine
    private val reminderScheduler: ReminderScheduler

    private var speechHelper: SpeechRecognitionHelper? = null
    private var ttsHelper: TextToSpeechHelper? = null

    // UI States
    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _liveTranscription = MutableStateFlow("")
    val liveTranscription: StateFlow<String> = _liveTranscription.asStateFlow()

    private val _activeTab = MutableStateFlow(AppTab.DASHBOARD)
    val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

    private val _aiMode = MutableStateFlow("hybrid") // "hybrid", "offline", "online"
    val aiMode: StateFlow<String> = _aiMode.asStateFlow()

    private val _language = MutableStateFlow("hi-IN")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo(100, false, "Unknown"))
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _confirmationDialog = MutableStateFlow<ConfirmationData?>(null)
    val confirmationDialog: StateFlow<ConfirmationData?> = _confirmationDialog.asStateFlow()

    private val _contactSelectionDialog = MutableStateFlow<ContactSelectionData?>(null)
    val contactSelectionDialog: StateFlow<ContactSelectionData?> = _contactSelectionDialog.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(1.0f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    // Data flows
    val allTasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val todayTasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

    init {
        val db = SadbhavnaDatabase.getDatabase(application)
        repository = SadbhavnaRepository(db)
        phoneManager = PhoneActionManager(application)
        intentEngine = SadbhavnaIntentEngine()
        reminderScheduler = ReminderScheduler(application)

        // Initialize Speech and TTS
        initVoiceEngines()

        // Observe Room data
        viewModelScope.launch {
            repository.allTasks.collect { tasks ->
                allTasks.value = tasks
            }
        }
        viewModelScope.launch {
            repository.getTodayTasks().collect { tasks ->
                todayTasks.value = tasks
            }
        }
        viewModelScope.launch {
            repository.allMessages.collect { messages ->
                chatMessages.value = messages
            }
        }

        refreshDeviceStatus()
    }

    private fun initVoiceEngines() {
        val app = getApplication<Application>()
        speechHelper = SpeechRecognitionHelper(
            context = app,
            onStateChange = { state -> _assistantState.value = state },
            onPartialResult = { text -> _liveTranscription.value = text },
            onFinalResult = { text ->
                _liveTranscription.value = text
                processCommand(text)
            },
            onError = { err ->
                _statusBanner.value = err
                _assistantState.value = AssistantState.IDLE
            }
        )

        ttsHelper = TextToSpeechHelper(
            context = app,
            onStateChange = { state ->
                // If TTS starts, set speaking; if finishes and was speaking, go idle
                if (state == AssistantState.SPEAKING) {
                    _assistantState.value = AssistantState.SPEAKING
                } else if (_assistantState.value == AssistantState.SPEAKING) {
                    _assistantState.value = AssistantState.IDLE
                }
            },
            onInitResult = { success ->
                if (success) {
                    ttsHelper?.setLanguage(_language.value)
                    ttsHelper?.setSpeedAndPitch(_speechRate.value, _speechPitch.value)
                }
            }
        )
    }

    fun refreshDeviceStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            _batteryInfo.value = phoneManager.getBatteryInfo()
            _isOnline.value = phoneManager.isOnline()
        }
    }

    fun setActiveTab(tab: AppTab) {
        _activeTab.value = tab
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    // --- Voice Controls ---

    fun startListening() {
        ttsHelper?.stop()
        _liveTranscription.value = ""
        speechHelper?.startListening(_language.value)
    }

    fun stopListening() {
        speechHelper?.stopListening()
    }

    fun stopSpeaking() {
        ttsHelper?.stop()
        _assistantState.value = AssistantState.IDLE
    }

    fun toggleListening() {
        if (_assistantState.value == AssistantState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    // --- Command Processing ---

    fun processCommand(input: String) {
        val query = input.trim()
        if (query.isEmpty()) return

        _assistantState.value = AssistantState.THINKING

        viewModelScope.launch {
            // Save User message
            repository.insertMessage(
                ChatMessage(
                    role = "user",
                    content = query
                )
            )

            val parsed = intentEngine.parse(query)
            handleParsedIntent(parsed, query)
        }
    }

    private suspend fun handleParsedIntent(intent: ParsedIntent, rawQuery: String) {
        when (intent) {
            is ParsedIntent.StopSpeaking -> {
                stopSpeaking()
                respond("Theek hai ji, main abhi chup ho jati hoon.", "STOP")
            }

            is ParsedIntent.GetTime -> {
                val res = phoneManager.getCurrentTimeReport()
                if (res is ActionResult.Success) {
                    respond(res.spokenFeedback, "TIME", res.details)
                }
            }

            is ParsedIntent.GetDate -> {
                val res = phoneManager.getCurrentDateReport()
                if (res is ActionResult.Success) {
                    respond(res.spokenFeedback, "DATE", res.details)
                }
            }

            is ParsedIntent.GetBattery -> {
                val res = phoneManager.getBatteryReportSpoken()
                if (res is ActionResult.Success) {
                    respond(res.spokenFeedback, "BATTERY", res.details)
                }
            }

            is ParsedIntent.OpenYouTube -> {
                val res = phoneManager.openYouTube()
                handleActionResult(res)
            }

            is ParsedIntent.OpenCamera -> {
                val res = phoneManager.openCamera()
                handleActionResult(res)
            }

            is ParsedIntent.OpenCalculator -> {
                val res = phoneManager.openCalculator()
                handleActionResult(res)
            }

            is ParsedIntent.OpenSettings -> {
                val res = phoneManager.openSettings()
                handleActionResult(res)
            }

            is ParsedIntent.OpenSettingSpecific -> {
                val res = when (intent.type) {
                    "wifi" -> phoneManager.openWifiSettings()
                    "bluetooth" -> phoneManager.openBluetoothSettings()
                    "sound" -> phoneManager.openSoundSettings()
                    "display" -> phoneManager.openDisplaySettings()
                    "location" -> phoneManager.openLocationSettings()
                    else -> phoneManager.openSettings()
                }
                handleActionResult(res)
            }

            is ParsedIntent.AdjustVolume -> {
                val dir = if (intent.isUp) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
                val res = phoneManager.adjustVolume(dir)
                handleActionResult(res)
            }

            is ParsedIntent.OpenMaps -> {
                val res = phoneManager.openMaps(intent.query)
                handleActionResult(res)
            }

            is ParsedIntent.ToggleAiMode -> {
                _aiMode.value = intent.mode
                val spoken = if (intent.mode == "offline") {
                    "Ji, main ab offline mode me aapki help karungi."
                } else {
                    "Ji, online intelligence mode active ho gaya hai."
                }
                respond(spoken, "MODE")
            }

            is ParsedIntent.CallContact -> {
                handleCallWorkflow(intent.contactName)
            }

            is ParsedIntent.SendSms -> {
                handleSmsWorkflow(intent.contactName, intent.messageText)
            }

            is ParsedIntent.CreateReminder -> {
                handleCreateReminder(intent)
            }

            is ParsedIntent.ReadTasks -> {
                handleReadTasks()
            }

            is ParsedIntent.GeneralAiQuery -> {
                handleAiQuery(rawQuery)
            }
        }
    }

    private suspend fun handleCallWorkflow(contactName: String) {
        val matches = phoneManager.searchContacts(contactName)
        if (matches.isEmpty()) {
            _confirmationDialog.value = ConfirmationData(
                title = "Contact nahi mila",
                description = "\"$contactName\" naam ka koi contact nahi mila ji. Kya aap direct number dial karna chahenge?",
                onConfirm = {
                    _confirmationDialog.value = null
                    phoneManager.openDialer("", contactName)
                },
                onCancel = { _confirmationDialog.value = null }
            )
            respond("Hmm... $contactName ka number contacts me nahi mila ji. Dialer open karoon?", "CALL")
        } else if (matches.size == 1) {
            val contact = matches[0]
            _confirmationDialog.value = ConfirmationData(
                title = "${contact.name} ko call karein?",
                description = "Number: ${contact.phoneNumber}\nJarvis dialer open karegi.",
                onConfirm = {
                    _confirmationDialog.value = null
                    val res = phoneManager.openDialer(contact.phoneNumber, contact.name)
                    handleActionResult(res)
                },
                onCancel = {
                    _confirmationDialog.value = null
                    respond("Theek hai ji, call cancel kar di gayi hai.", "CALL")
                }
            )
            respond("Achha ji, ${contact.name} ko call lagayein? Kripya confirm kar dijiye.", "CALL")
        } else {
            // Multiple contacts found
            _contactSelectionDialog.value = ContactSelectionData(
                title = "Contact chuniye ($contactName)",
                contacts = matches,
                onSelected = { selected ->
                    _contactSelectionDialog.value = null
                    val res = phoneManager.openDialer(selected.phoneNumber, selected.name)
                    handleActionResult(res)
                },
                onCancel = { _contactSelectionDialog.value = null }
            )
            respond("Ji, $contactName naam se ek se zyada contacts mile hain. Kripya sahi contact chunein.", "CALL")
        }
    }

    private suspend fun handleSmsWorkflow(contactName: String, body: String) {
        val matches = phoneManager.searchContacts(contactName)
        val number = if (matches.isNotEmpty()) matches[0].phoneNumber else contactName
        val targetName = if (matches.isNotEmpty()) matches[0].name else contactName

        _confirmationDialog.value = ConfirmationData(
            title = "$targetName ko Message bhejein?",
            description = "Message: \"$body\"\nTarget: $number",
            onConfirm = {
                _confirmationDialog.value = null
                val res = phoneManager.prepareSms(number, body, targetName)
                handleActionResult(res)
            },
            onCancel = {
                _confirmationDialog.value = null
                respond("Theek hai ji, message cancel kar diya.", "SMS")
            }
        )
        respond("Ji, $targetName ke liye ye message taiyaar hai: \"$body\". Confirm kar dijiye ji.", "SMS")
    }

    private suspend fun handleCreateReminder(intent: ParsedIntent.CreateReminder) {
        val calendar = Calendar.getInstance()
        if (intent.hourOfDay != null) {
            calendar.set(Calendar.HOUR_OF_DAY, intent.hourOfDay)
            calendar.set(Calendar.MINUTE, intent.minute ?: 0)
            calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                // If the time already passed today, schedule for tomorrow
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            // Default 1 hour from now
            calendar.add(Calendar.HOUR_OF_DAY, 1)
        }

        val task = TaskItem(
            title = intent.title,
            description = "Jarvis Voice Reminder",
            category = intent.category,
            dueTimestamp = calendar.timeInMillis,
            hasReminder = true,
            priority = "High"
        )

        val id = repository.insertTask(task)
        val savedTask = task.copy(id = id)
        reminderScheduler.scheduleTaskReminder(savedTask)

        val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(calendar.time)
        val feedback = "Theek hai ji! Maine \"${intent.title}\" ka reminder $timeStr ke liye set kar diya hai. Aap bilkul chinta mat kijiye."
        respond(feedback, "REMINDER")
    }

    private suspend fun handleReadTasks() {
        val tasks = todayTasks.value.filter { !it.isCompleted }
        if (tasks.isEmpty()) {
            respond("Ji, aaj aapka koi pending task nahi hai. Ekdum relaxed rahiye!", "TASKS")
        } else {
            val listText = tasks.take(3).joinToString(", ") { it.title }
            val spoken = "Hmm... Aaj aapke ${tasks.size} tasks hain ji. Jaise: $listText."
            respond(spoken, "TASKS", "Total pending: ${tasks.size}")
        }
    }

    private suspend fun handleAiQuery(query: String) {
        val currentMode = _aiMode.value
        val isNetAvailable = phoneManager.isOnline()

        if (currentMode == "offline" || !isNetAvailable) {
            // Offline intelligent response
            val offlineAnswer = generateOfflineKnowledgeResponse(query)
            respond(offlineAnswer, "AI_OFFLINE")
            return
        }

        // Online Gemini AI query
        val result = GeminiClient.askGemini(query)
        result.onSuccess { answer ->
            respond(answer, "AI_ONLINE")
        }.onFailure { error ->
            Log.e("SadbhavnaVM", "Gemini error: ${error.message}")
            val fallback = "Sorry ji, online brain se connect hone me thodi problem aayi. Par main offline mode me aapki poori help karne ke liye tayar hoon."
            respond(fallback, "AI_ERROR", isSuccess = false)
        }
    }

    private fun generateOfflineKnowledgeResponse(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("kaun ho") || q.contains("who are you") || q.contains("tumhara naam") ->
                "Namaste ji! Main Jarvis hoon. Main ek soft-spoken, polite aur friendly Indian ladki ki tarah aapki help karti hoon. Aapke daily tasks, phone operations, reminders aur study questions me main hamesha aapke saath hoon."
            q.contains("science") || q.contains("gravitation") || q.contains("force") ->
                "Achha suniye ji... Class 9 Science me gravitation ek natural aakarshan bal (attractive force) hai jo do objects ke beech lagta hai. Newton ke niyam ke according F = G*(m1*m2)/r^2 hota hai."
            q.contains("photosynthesis") ->
                "Hmm... Photosynthesis paudhon ka apna khana banane ka process hai ji, jisme sunlight, carbon dioxide aur paani milkar glucose aur oxygen banate hain."
            q.contains("kya kar sakte ho") || q.contains("features") || q.contains("help") ->
                "Ji! Main aapke liye YouTube khol sakti hoon, camera open kar sakti hoon, reminders set kar sakti hoon, phone calls aur SMS draft kar sakti hoon, aur aapke sawaalon ke answers de sakti hoon."
            else ->
                "Hmm... Main samajh gayi ji. Offline engine me maine ye note kar liya hai. Agar aapko iske baare me aur detail me janna ho, toh aap Settings me online brain on kar sakte hain ji."
        }
    }

    private fun handleActionResult(res: ActionResult) {
        when (res) {
            is ActionResult.Success -> respond(res.spokenFeedback, res.actionType, res.details, true)
            is ActionResult.Failure -> respond(res.spokenFeedback, "ERROR", res.errorDetail, false)
            is ActionResult.NeedsConfirmation -> {
                _confirmationDialog.value = ConfirmationData(
                    title = res.title,
                    description = res.description,
                    onConfirm = {
                        _confirmationDialog.value = null
                        res.confirmAction()
                    },
                    onCancel = {
                        _confirmationDialog.value = null
                        res.cancelAction()
                    }
                )
            }
            is ActionResult.ContactSelectionRequired -> {
                _contactSelectionDialog.value = ContactSelectionData(
                    title = "Choose Contact",
                    contacts = res.contacts,
                    onSelected = {
                        _contactSelectionDialog.value = null
                        res.onSelected(it)
                    }
                )
            }
        }
    }

    private fun respond(text: String, actionType: String? = null, actionDetails: String? = null, isSuccess: Boolean = true) {
        viewModelScope.launch {
            repository.insertMessage(
                ChatMessage(
                    role = "assistant",
                    content = text,
                    actionType = actionType,
                    actionDetails = actionDetails,
                    isSuccess = isSuccess
                )
            )
            ttsHelper?.speak(text)
        }
    }

    // --- Task Operations ---

    fun addTask(title: String, description: String, category: String, dueTimestamp: Long, hasReminder: Boolean) {
        viewModelScope.launch {
            val task = TaskItem(
                title = title,
                description = description,
                category = category,
                dueTimestamp = dueTimestamp,
                hasReminder = hasReminder
            )
            val id = repository.insertTask(task)
            if (hasReminder) {
                reminderScheduler.scheduleTaskReminder(task.copy(id = id))
            }
            _statusBanner.value = "Task \"$title\" add ho gaya!"
        }
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updated)
            if (updated.isCompleted) {
                reminderScheduler.cancelTaskReminder(task.id)
            } else if (updated.hasReminder) {
                reminderScheduler.scheduleTaskReminder(updated)
            }
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            reminderScheduler.cancelTaskReminder(task.id)
            repository.deleteTask(task)
            _statusBanner.value = "Task delete ho gaya."
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            _statusBanner.value = "Chat history clear ho gayi."
        }
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        ttsHelper?.setLanguage(lang)
        viewModelScope.launch {
            repository.setPreference("assistant_language", lang)
        }
    }

    fun setAiMode(mode: String) {
        _aiMode.value = mode
        viewModelScope.launch {
            repository.setPreference("ai_mode", mode)
        }
    }

    fun setSpeechSettings(rate: Float, pitch: Float) {
        _speechRate.value = rate
        _speechPitch.value = pitch
        ttsHelper?.setSpeedAndPitch(rate, pitch)
        viewModelScope.launch {
            repository.setPreference("speech_rate", rate.toString())
            repository.setPreference("speech_pitch", pitch.toString())
        }
    }

    fun speakText(text: String) {
        ttsHelper?.speak(text)
    }

    // Direct Phone Action Shortcuts
    fun triggerAppAction(actionName: String) {
        when (actionName) {
            "YOUTUBE" -> handleActionResult(phoneManager.openYouTube())
            "CAMERA" -> handleActionResult(phoneManager.openCamera())
            "CALCULATOR" -> handleActionResult(phoneManager.openCalculator())
            "SETTINGS" -> handleActionResult(phoneManager.openSettings())
            "WIFI" -> handleActionResult(phoneManager.openWifiSettings())
            "BLUETOOTH" -> handleActionResult(phoneManager.openBluetoothSettings())
            "MAPS" -> handleActionResult(phoneManager.openMaps("Nearby"))
            "BATTERY" -> handleActionResult(phoneManager.getBatteryReportSpoken())
            "TIME" -> handleActionResult(phoneManager.getCurrentTimeReport())
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper?.destroy()
        ttsHelper?.shutdown()
    }
}
