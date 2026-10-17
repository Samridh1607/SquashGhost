package com.squashghosting.trainer.ui

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.squashghosting.trainer.audio.BeepPlayer
import com.squashghosting.trainer.audio.CoachSpeaker
import com.squashghosting.trainer.core.CourtPosition
import com.squashghosting.trainer.core.Pattern
import com.squashghosting.trainer.core.SequenceGenerator
import com.squashghosting.trainer.core.VoiceGender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TrainingUiState(
    val pattern: Pattern = Pattern.DEFAULT,
    val voice: VoiceGender = VoiceGender.DEFAULT,
    val totalSets: Int = 5,
    val delayBetweenSets: Int = 10,
    val delayBetweenInstructions: Float = 3f,
    val isTraining: Boolean = false,
    val status: String = STATUS_READY,
    val progress: String = "",
) {
    companion object {
        const val STATUS_READY = "Ready to start training"
    }
}

/**
 * Owns the training session. Lives across configuration changes, so a
 * rotation never interrupts a set. Timing runs on the main looper; a partial
 * wake lock keeps the CPU (and therefore the calls) alive if the screen
 * turns off.
 */
class TrainingViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TrainingUiState())
    val state: StateFlow<TrainingUiState> = _state.asStateFlow()

    private val speaker = CoachSpeaker(application)
    private val beeps = BeepPlayer()
    private val generator = SequenceGenerator()

    private val handler = Handler(Looper.getMainLooper())
    private val eventToken = Any() // all pending events are posted with this token

    private var wakeLock: PowerManager.WakeLock? = null

    // Session progress
    private var currentSet = 0
    private var positionIndex = 0
    private var positions: List<CourtPosition> = emptyList()

    private val s get() = _state.value

    // ------------------------------------------------------------------
    // Settings
    // ------------------------------------------------------------------
    fun setPattern(pattern: Pattern) = _state.update { it.copy(pattern = pattern) }

    /** Can be changed live; applies to the next call. Previews when idle. */
    fun setVoice(voice: VoiceGender) {
        _state.update { it.copy(voice = voice) }
        speaker.voice = voice
        if (!s.isTraining) {
            // Preview with a real call so the player hears the actual texture
            speaker.speak(CourtPosition.FRONT_RIGHT.id)
        }
    }

    fun setTotalSets(value: Int) = _state.update { it.copy(totalSets = value) }

    fun setDelayBetweenSets(value: Int) = _state.update { it.copy(delayBetweenSets = value) }

    /** Can be changed live; applies from the next call. */
    fun setDelayBetweenInstructions(value: Float) =
        _state.update { it.copy(delayBetweenInstructions = value) }

    // ------------------------------------------------------------------
    // Scheduling helpers
    // ------------------------------------------------------------------
    private fun schedule(delaySeconds: Double, action: () -> Unit) {
        handler.postAtTime(
            { action() },
            eventToken,
            SystemClock.uptimeMillis() + (delaySeconds * 1000).toLong()
        )
    }

    private fun cancelAllEvents() = handler.removeCallbacksAndMessages(eventToken)

    private fun setProgress(text: String) = _state.update { it.copy(progress = text) }

    // ------------------------------------------------------------------
    // Training flow
    // ------------------------------------------------------------------
    fun startTraining() {
        if (s.isTraining) return
        currentSet = 1
        positionIndex = 0
        generator.reset()
        _state.update {
            it.copy(isTraining = true, status = "Training in progress... (${it.pattern.displayName})")
        }
        acquireWakeLock()
        countdownTick(COUNTDOWN_SECONDS)
    }

    /** Beep once per second, then start the first set. */
    private fun countdownTick(remaining: Int) {
        if (!s.isTraining) return
        if (remaining <= 0) {
            startNewSet()
            return
        }
        setProgress("Starting in $remaining...")
        beeps.beep(high = false)
        schedule(1.0) { countdownTick(remaining - 1) }
    }

    private fun startNewSet() {
        if (!s.isTraining) return
        if (currentSet > s.totalSets) {
            completeTraining()
            return
        }
        positions = generator.nextSet(s.pattern, currentSet)
        positionIndex = 0

        beeps.beep(high = true) // long high beep marks the start of each set
        setProgress("Set $currentSet/${s.totalSets} - Starting...")
        schedule(1.0) { speakNextPosition() }
    }

    private fun speakNextPosition() {
        if (!s.isTraining) return
        if (positionIndex >= positions.size) {
            completeCurrentSet()
            return
        }
        val position = positions[positionIndex]
        positionIndex++

        setProgress(
            "Set $currentSet/${s.totalSets} - " +
                "Position $positionIndex/${positions.size}: ${position.label}"
        )
        speaker.speak(position.id)
        schedule(s.delayBetweenInstructions.toDouble()) { speakNextPosition() }
    }

    private fun completeCurrentSet() {
        if (!s.isTraining) return
        currentSet++
        if (currentSet > s.totalSets) {
            completeTraining()
            return
        }
        setProgress(
            "Set ${currentSet - 1} completed. " +
                "Next set starting in ${s.delayBetweenSets} seconds..."
        )
        schedule(s.delayBetweenSets.toDouble()) { startNewSet() }
    }

    private fun completeTraining() {
        if (s.isTraining) {
            setProgress("Training completed! Well done!")
            speaker.speak("Training completed. Well done!")
            schedule(4.0) { resetUi() }
        } else {
            resetUi()
        }
    }

    fun endTraining() {
        _state.update { it.copy(isTraining = false) }
        cancelAllEvents()
        speaker.stop()
        resetUi()
    }

    private fun resetUi() {
        releaseWakeLock()
        _state.update {
            it.copy(isTraining = false, status = TrainingUiState.STATUS_READY, progress = "")
        }
    }

    // ------------------------------------------------------------------
    // Partial wake lock (screen-on flag is handled by the Activity)
    // ------------------------------------------------------------------
    private fun acquireWakeLock() {
        try {
            val lock = wakeLock ?: (getApplication<Application>()
                .getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SquashGhosting::session")
                .also {
                    it.setReferenceCounted(false)
                    wakeLock = it
                }
            // Safety timeout (2 h) so a crash can never leak the lock forever
            if (!lock.isHeld) lock.acquire(WAKE_LOCK_TIMEOUT_MS)
        } catch (e: Exception) {
            Log.w(TAG, "Could not acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.takeIf { it.isHeld }?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Could not release wake lock", e)
        }
    }

    // ------------------------------------------------------------------
    // Lifecycle (equivalent of Kivy's on_stop)
    // ------------------------------------------------------------------
    override fun onCleared() {
        _state.update { it.copy(isTraining = false) }
        cancelAllEvents()
        releaseWakeLock()
        speaker.shutdown()
        beeps.release()
        super.onCleared()
    }

    companion object {
        private const val TAG = "TrainingViewModel"
        const val COUNTDOWN_SECONDS = 3
        private const val WAKE_LOCK_TIMEOUT_MS = 2L * 60 * 60 * 1000
    }
}
