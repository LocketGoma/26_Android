package kr.ac.lecture.mobilegame.game.addon

import android.view.KeyEvent
import kr.ac.lecture.mobilegame.foundation.input.DigitalInput
import kr.ac.lecture.mobilegame.foundation.input.GameAction

/** 플랫포머용 선택 애드온: A/D 이동, W 점프를 기존 조작 입력에 연결합니다. */
class PlatformerWasdControls(private val controls: DigitalInput) {
    fun onKey(event: KeyEvent, enabled: Boolean): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN && event.action != KeyEvent.ACTION_UP) return false
        val action = when (event.keyCode) {
            KeyEvent.KEYCODE_A -> GameAction.LEFT
            KeyEvent.KEYCODE_D -> GameAction.RIGHT
            KeyEvent.KEYCODE_W -> GameAction.JUMP
            KeyEvent.KEYCODE_S -> return true // S는 기능을 부여하지 않고 입력만 소비합니다.
            else -> return false
        }
        // 키 자동 반복은 새 점프로 취급하지 않습니다.
        if (enabled && (event.action == KeyEvent.ACTION_UP || event.repeatCount == 0)) {
            controls.setKeyboard(action, event.action == KeyEvent.ACTION_DOWN)
        }
        return true
    }
}
