package com.squashghosting.trainer.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Short sine-wave beeps synthesised in memory (same tones as the Python app:
 * 660 Hz / 150 ms "low" countdown beep, 1000 Hz / 400 ms "high" set-start beep).
 */
class BeepPlayer {

    private val low: AudioTrack? = createTone(freq = 660.0, ms = 150)
    private val high: AudioTrack? = createTone(freq = 1000.0, ms = 400)

    fun beep(high: Boolean = false) {
        val track = (if (high) this.high else low) ?: return
        try {
            // Restart from the beginning even if the previous beep is still playing
            track.stop()
            track.reloadStaticData()
            track.play()
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Could not play beep", e)
        }
    }

    fun release() {
        low?.release()
        high?.release()
    }

    private fun createTone(
        freq: Double,
        ms: Int,
        volume: Double = 0.6,
        sampleRate: Int = SAMPLE_RATE,
    ): AudioTrack? = try {
        val pcm = synthesize(freq, ms, volume, sampleRate)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        track.write(pcm, 0, pcm.size)
        track
    } catch (e: Exception) {
        Log.e(TAG, "Could not create beep sound", e)
        null
    }

    companion object {
        private const val TAG = "BeepPlayer"
        private const val SAMPLE_RATE = 22050

        /** 16-bit mono PCM sine wave with a short fade in/out to avoid clicks. */
        fun synthesize(freq: Double, ms: Int, volume: Double, sampleRate: Int): ShortArray {
            val n = sampleRate * ms / 1000
            val fade = min(200, n / 2)
            return ShortArray(n) { i ->
                val env = if (fade > 0) {
                    minOf(1.0, i.toDouble() / fade, (n - i).toDouble() / fade)
                } else 1.0
                (32767 * volume * env * sin(2 * PI * freq * i / sampleRate)).toInt().toShort()
            }
        }
    }
}
