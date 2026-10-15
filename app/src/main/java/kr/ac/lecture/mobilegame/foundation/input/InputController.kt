package kr.ac.lecture.mobilegame.foundation.input

import android.view.MotionEvent
import kr.ac.lecture.mobilegame.foundation.graphics.FitViewport

/**
 * 터치와 센서 Callback에서 수집한 입력 상태를 담는 immutable data class입니다.
 * 현재 SensorInput은 Main Thread에서 등록하므로 별도 Sensor Thread를 만들지 않습니다.
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

    @Synchronized fun onTouch(event: MotionEvent, width: Int, height: Int,
                             area: FitViewport = FitViewport(0, 0, width.coerceAtLeast(1), height.coerceAtLeast(1))): Boolean {
        if (width <= 0 || height <= 0) return false
        val point = area.toNdc(event.x, event.y)
        if (point == null) { clearTouch(); return true }
        latest = latest.copy(
            touching = event.actionMasked != MotionEvent.ACTION_UP &&
                event.actionMasked != MotionEvent.ACTION_CANCEL,
            worldX = point.first,
            worldY = point.second,
        )
        return true
    }

    @Synchronized fun clearTouch() { latest = latest.copy(touching = false) }

    @Synchronized fun updateAccelerometer(x: Float, y: Float) {
        latest = latest.copy(accelerometerX = x, accelerometerY = y)
    }

    @Synchronized fun updateGyroscope(z: Float) {
        latest = latest.copy(gyroscopeZ = z)
    }
}
