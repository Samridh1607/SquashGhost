package com.squashghosting.trainer.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import com.squashghosting.trainer.core.VoiceGender
import com.squashghosting.trainer.core.VoiceNames
import java.util.Locale

/**
 * Wraps ONE [TextToSpeech] instance; speaks only after onInit(SUCCESS).
 * The selected voice gender can be changed at any time and is applied lazily
 * on the next call.
 */
class CoachSpeaker(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context.applicationContext, this)

    @Volatile
    private var ready = false

    /** Gender last applied to the engine. */
    private var appliedVoice: VoiceGender? = null

    /** Gender to use for the next call. */
    var voice: VoiceGender = VoiceGender.DEFAULT

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        Log.i(TAG, "Android TTS init status: $status")
    }

    fun speak(text: String) {
        val speechText = text.replace('-', ' ')
        if (!ready) {
            Log.w(TAG, "Android TTS not ready yet")
            return
        }
        try {
            if (appliedVoice != voice) applyVoice()
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            // speak() is non-blocking
            tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Android TTS error for '$speechText'", e)
        }
    }

    fun stop() {
        try {
            tts.stop()
        } catch (e: Exception) {
            Log.w(TAG, "TTS stop error", e)
        }
    }

    fun shutdown() {
        try {
            tts.stop()
            tts.shutdown()
        } catch (e: Exception) {
            Log.w(TAG, "TTS shutdown error", e)
        }
    }

    /**
     * Select the most natural offline English voice of the chosen gender.
     *
     * Ranking: device's own country first (accent the player understands
     * best), then genuine voices over legacy '#male_1' style variants, then
     * the engine's quality rating. A small pitch/rate touch-up is applied on
     * top; heavy pitch shifting is used only if no matching voice exists.
     */
    private fun applyVoice() {
        val gender = voice
        tts.setLanguage(Locale.ENGLISH) // known state (also resets voice)
        setSpeechAudioAttributes()

        val chosen: Voice? = try {
            val defaultCountry = Locale.getDefault().country
            tts.voices.orEmpty()
                .asSequence()
                .filter { it.locale.language == "en" && !it.isNetworkConnectionRequired }
                // Skip voices whose data is not installed on the device
                .filter { TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() }
                .filter { VoiceNames.genderFromName(it.name) == gender }
                .sortedWith(
                    compareBy<Voice>(
                        { it.locale.country != defaultCountry },
                        { '#' in it.name },          // legacy pitched variant -> lower priority
                        { -it.quality },
                        { it.latency },
                        { it.name },
                    )
                )
                .firstOrNull()
        } catch (e: Exception) {
            Log.w(TAG, "Voice lookup failed", e)
            null
        }

        if (chosen != null) {
            tts.setVoice(chosen)
            tts.setPitch(gender.pitch)
            Log.i(TAG, "Android TTS voice: ${chosen.name}")
        } else {
            tts.setPitch(gender.fallbackPitch)
            Log.i(TAG, "No ${gender.key} voice found; approximating with pitch")
        }
        tts.setSpeechRate(gender.rate)
        appliedVoice = gender
    }

    /** Tag the output as speech so the system applies speech processing. */
    private fun setSpeechAudioAttributes() {
        try {
            tts.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not set TTS audio attributes", e)
        }
    }

    companion object {
        private const val TAG = "CoachSpeaker"
        private const val UTTERANCE_ID = "ghosting"
    }
}
