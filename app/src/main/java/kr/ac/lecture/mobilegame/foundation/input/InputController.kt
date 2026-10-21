package kr.ac.lecture.mobilegame.foundation.input

import android.view.MotionEvent
import android.view.KeyEvent
import kr.ac.lecture.mobilegame.foundation.graphics.FitViewport

/**
 * 한 프레임에서 읽을 터치·키보드·센서 입력입니다. 전달한 값은 나중에 수정하지 않습니다.
 * UI 스레드가 새 값을 만들고 GL 스레드가 snapshot()으로 읽습니다.
 */
data class InputSnapshot(
    val touching: Boolean = false,
    val worldX: Float = 0f,
    val worldY: Float = 0f,
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val gyroscopeZ: Float = 0f,
    val horizontalAxis: Float = 0f,
    val verticalAxis: Float = 0f,
)

/** 터치·키보드·센서 콜백의 최신 값을 보관합니다. GL 스레드는 입력만 읽습니다. */
open class InputController {
    @Volatile
    private var latest = InputSnapshot()
    private var pointerId = -1

    open fun snapshot(): InputSnapshot = latest

    /** 키보드 애드온에서 필요한 키만 처리하도록 재정의합니다. */
    open fun onKey(event: KeyEvent): Boolean = false

    @Synchronized
    fun onTouch(
        event: MotionEvent, width: Int, height: Int, area: FitViewport = FitViewport(0, 0, width.coerceAtLeast(1), height.coerceAtLeast(1))
    ): Boolean {
        if (width <= 0 || height <= 0) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> pointerId = event.getPointerId(event.actionIndex)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                clearTouch(); return true
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) == pointerId) clearTouch()
                return true
            }
        }
        // 첫 손가락을 놓았을 때 다른 손가락 위치로 갑자기 목표가 바뀌지 않게 합니다.
        val index = event.findPointerIndex(pointerId)
        if (index < 0) return true
        val point = area.toNdc(event.getX(index), event.getY(index))
        if (point == null) {
            latest = latest.copy(touching = false); return true
        }
        latest = latest.copy(touching = true, worldX = point.first, worldY = point.second)
        return true
    }

    @Synchronized
    fun clearTouch() {
        pointerId = -1; latest = latest.copy(touching = false)
    }

    @Synchronized
    open fun clear() {
        clearTouch()
    }

    @Synchronized
    fun updateAccelerometer(x: Float, y: Float) {
        latest = latest.copy(accelerometerX = x, accelerometerY = y)
    }

    @Synchronized
    fun updateGyroscope(z: Float) {
        latest = latest.copy(gyroscopeZ = z)
    }
}
