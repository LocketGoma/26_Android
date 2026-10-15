package kr.ac.lecture.mobilegame.foundation.input

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/** 센서 이벤트를 InputController에 기록합니다. 현재 Player 이동에는 터치를 사용합니다. */
class SensorInput(context: Context, private val input: InputController) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = manager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private var started = false

    fun start() {
        if (started) return
        val manager = manager ?: return
        started = true
        accelerometer?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        gyroscope?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun stop() {
        if (!started) return
        manager?.unregisterListener(this)
        started = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> input.updateAccelerometer(event.values[0], event.values[1])
            Sensor.TYPE_GYROSCOPE -> input.updateGyroscope(event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
