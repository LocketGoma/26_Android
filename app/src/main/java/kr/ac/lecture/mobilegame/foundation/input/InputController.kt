package kr.ac.lecture.mobilegame.foundation.input

import android.view.MotionEvent

/** UI Thread의 입력을 Render Thread가 안전하게 읽을 수 있는 불변 스냅샷으로 전달합니다. */
class InputController {
    @Volatile private var latest = InputSnapshot()

    fun snapshot(): InputSnapshot = latest

    fun onTouch(event: MotionEvent, width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        val x = event.x / width * 2f - 1f
        val y = 1f - event.y / height * 2f
        latest = latest.copy(
            touching = event.actionMasked != MotionEvent.ACTION_UP &&
                event.actionMasked != MotionEvent.ACTION_CANCEL,
            worldX = x.coerceIn(-1f, 1f),
            worldY = y.coerceIn(-1f, 1f),
        )
        return true
    }

    fun updateAccelerometer(x: Float, y: Float) {
        latest = latest.copy(accelerometerX = x, accelerometerY = y)
    }

    fun updateGyroscope(z: Float) {
        latest = latest.copy(gyroscopeZ = z)
    }
}
