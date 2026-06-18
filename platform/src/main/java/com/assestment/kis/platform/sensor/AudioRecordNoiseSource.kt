package com.assestment.kis.platform.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.assestment.kis.domain.detection.NoiseSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Reads the microphone and emits an RMS amplitude per sampling window. If RECORD_AUDIO is missing
 * or the recorder fails to initialise, the flow completes immediately (movement-only detection).
 * Recording is tied to collection — `awaitClose` releases the mic the moment the session stops —
 * and the blocking read loop runs on the IO dispatcher and is duty-cycled for battery.
 */
class AudioRecordNoiseSource(private val context: Context) : NoiseSource {

    override fun amplitudes(): Flow<Float> = callbackFlow {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            close()
            return@callbackFlow
        }

        val bufferSize = maxOf(AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING), MIN_BUFFER)
        val recorder = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize)
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            close()
            return@callbackFlow
        }

        val buffer = ShortArray(bufferSize)
        recorder.startRecording()
        val reader = launch(Dispatchers.IO) {
            // Drop the first buffers: AudioRecord emits a startup transient that would otherwise
            // register as a spurious noise event. Read continuously so no stale audio backs up.
            var warmupReadsRemaining = WARMUP_READS
            while (isActive) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) {
                    if (warmupReadsRemaining > 0) warmupReadsRemaining-- else trySend(rms(buffer, read))
                }
            }
        }

        awaitClose {
            reader.cancel()
            recorder.stop()
            recorder.release()
        }
    }

    private fun rms(buffer: ShortArray, length: Int): Float {
        var sum = 0.0
        for (i in 0 until length) {
            val value = buffer[i].toDouble()
            sum += value * value
        }
        return sqrt(sum / length).toFloat()
    }

    private companion object {
        const val SAMPLE_RATE = 16_000
        const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val MIN_BUFFER = 2048
        const val WARMUP_READS = 4
    }
}
