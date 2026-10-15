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
    private var pointerId = -1

    fun snapshot(): InputSnapshot = latest

    @Synchronized fun onTouch(event: MotionEvent, width: Int, height: Int,
                             area: FitViewport = FitViewport(0, 0, width.coerceAtLeast(1), height.coerceAtLeast(1))): Boolean {
        if (width <= 0 || height <= 0) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> pointerId = event.getPointerId(event.actionIndex)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { clearTouch(); return true }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) == pointerId) clearTouch()
                return true
            }
        }
        // 첫 손가락을 놓았을 때 다른 손가락 위치로 갑자기 목표가 바뀌지 않게 합니다.
        val index = event.findPointerIndex(pointerId)
        if (index < 0) return true
        val point = area.toNdc(event.getX(index), event.getY(index))
        if (point == null) { latest = latest.copy(touching = false); return true }
        latest = latest.copy(
            touching = true,
            worldX = point.first,
            worldY = point.second,
        )
        return true
    }

    @Synchronized fun clearTouch() { pointerId = -1; latest = latest.copy(touching = false) }

    @Synchronized fun updateAccelerometer(x: Float, y: Float) {
        latest = latest.copy(accelerometerX = x, accelerometerY = y)
    }

    @Synchronized fun updateGyroscope(z: Float) {
        latest = latest.copy(gyroscopeZ = z)
    }
}
