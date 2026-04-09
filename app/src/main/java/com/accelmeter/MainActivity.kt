package com.accelmeter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.LinkedList

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var tvX: TextView
    private lateinit var tvY: TextView
    private lateinit var tvZ: TextView

    // Moving average filter with 15-point window
    private val filterSize = 15
    private val bufferX = LinkedList<Float>()
    private val bufferY = LinkedList<Float>()
    private val bufferZ = LinkedList<Float>()

    // Lock protecting both the moving-average buffers and the latest filtered values.
    // onSensorChanged (sensor thread) writes; the UI runnable (main thread) reads.
    private val accelLock = Any()
    private data class AccelValues(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f)
    private var accelValues = AccelValues()

    private val handler = Handler(Looper.getMainLooper())
    private val updateIntervalMs = 100L

    private val uiUpdateRunnable = object : Runnable {
        override fun run() {
            val values = synchronized(accelLock) { accelValues }
            tvX.text = "X: %.1f".format(values.x)
            tvY.text = "Y: %.1f".format(values.y)
            tvZ.text = "Z: %.1f".format(values.z)
            handler.postDelayed(this, updateIntervalMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvX = findViewById(R.id.tvX)
        tvY = findViewById(R.id.tvY)
        tvZ = findViewById(R.id.tvZ)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        handler.post(uiUpdateRunnable)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(uiUpdateRunnable)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            synchronized(accelLock) {
                val x = movingAverage(bufferX, event.values[0])
                val y = movingAverage(bufferY, event.values[1])
                val z = movingAverage(bufferZ, event.values[2])
                accelValues = AccelValues(x, y, z)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }

    private fun movingAverage(buffer: LinkedList<Float>, newValue: Float): Float {
        buffer.addLast(newValue)
        if (buffer.size > filterSize) {
            buffer.removeFirst()
        }
        return buffer.sum() / buffer.size
    }
}
