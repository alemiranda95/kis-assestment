package com.assestment.kis.platform.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.assestment.kis.domain.detection.MotionSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Emits movement magnitude (total acceleration minus gravity) from the accelerometer at a modest
 * sampling rate for battery. The listener is registered on collection and unregistered when the
 * flow is cancelled (awaitClose), so the sensor is only active during a session.
 */
class SensorManagerMotionSource(private val context: Context) : MotionSource {

    override fun magnitudes(): Flow<Float> = callbackFlow {
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y, z) = event.values
                val magnitude = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
                trySend(abs(magnitude))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
