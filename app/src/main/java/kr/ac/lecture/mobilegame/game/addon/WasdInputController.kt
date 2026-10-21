package kr.ac.lecture.mobilegame.game.addon

import android.view.KeyEvent
import kr.ac.lecture.mobilegame.foundation.input.InputController
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot

/** 슈팅용 선택 애드온: 기존 터치·센서 입력에 WASD 이동 축을 추가합니다. */
class WasdInputController : InputController() {
    private val heldKeys = mutableSetOf<Int>()

    @Synchronized
    override fun onKey(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_D -> Unit
            else -> return false
        }
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (event.repeatCount == 0) heldKeys.add(event.keyCode)
            KeyEvent.ACTION_UP -> heldKeys.remove(event.keyCode)
            else -> return false
        }
        return true
    }

    @Synchronized
    override fun snapshot(): InputSnapshot = super.snapshot().copy(horizontalAxis = (if (KeyEvent.KEYCODE_D in heldKeys) 1f else 0f) - (if (KeyEvent.KEYCODE_A in heldKeys) 1f else 0f), verticalAxis = (if (KeyEvent.KEYCODE_W in heldKeys) 1f else 0f) - (if (KeyEvent.KEYCODE_S in heldKeys) 1f else 0f))

    @Synchronized
    override fun clear() {
        super.clear()
        heldKeys.clear()
    }
}
