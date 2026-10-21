package kr.ac.lecture.mobilegame.foundation.input

// MELEE는 기반 기능 확장용이며 현재 플랫포머 조작부는 LEFT/RIGHT/JUMP만 연결합니다.
enum class GameAction { LEFT, RIGHT, JUMP, MELEE }

data class DigitalSnapshot(val axis: Float = 0f, val jumpPressed: Boolean = false, val meleePressed: Boolean = false)

/** UI Thread는 터치·키보드 버튼을 기록하고 GL Thread는 프레임마다 읽습니다. */
class DigitalInput {
    private val held = mutableSetOf<GameAction>()
    private val keyboardHeld = mutableSetOf<GameAction>()
    private var jump = false
    private var melee = false

    @Synchronized
    fun set(action: GameAction, pressed: Boolean) {
        setHeld(held, action, pressed)
    }

    @Synchronized
    fun setKeyboard(action: GameAction, pressed: Boolean) {
        setHeld(keyboardHeld, action, pressed)
    }

    private fun setHeld(source: MutableSet<GameAction>, action: GameAction, pressed: Boolean) {
        val wasHeld = isHeld(action)
        if (pressed) source.add(action) else source.remove(action)
        if (pressed && !wasHeld) {
            if (action == GameAction.JUMP) jump = true
            if (action == GameAction.MELEE) melee = true
        }
    }

    private fun isHeld(action: GameAction) = action in held || action in keyboardHeld

    @Synchronized
    fun drain(): DigitalSnapshot {
        val axis = (if (isHeld(GameAction.RIGHT)) 1f else 0f) - (if (isHeld(GameAction.LEFT)) 1f else 0f)
        return DigitalSnapshot(axis, jump, melee).also { jump = false; melee = false }
    }

    @Synchronized
    fun clear() {
        held.clear(); keyboardHeld.clear(); jump = false; melee = false
    }
}
