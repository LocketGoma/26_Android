package kr.ac.lecture.mobilegame.foundation.input

import android.view.MotionEvent

/**
 * UI/Sensor Thread에서 수집한 입력 상태를 담는 immutable data class입니다.
 * InputController가 생성하고 Game Loop가 읽는 데이터이므로 같은 파일에 둡니다.
 */
data class InputSnapshot(
    val touching: Boolean = false,
    val worldX: Float = 0f,
    val worldY: Float = 0f,
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val gyroscopeZ: Float = 0f,
)

/** UI Thread의 입력을 immutable InputSnapshot으로 만들어 Render Thread에 전달합니다. */
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
