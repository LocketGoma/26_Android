package kr.ac.lecture.mobilegame.foundation.input

enum class GameAction { LEFT, RIGHT, JUMP, MELEE }

data class DigitalSnapshot(val axis: Float = 0f, val jumpPressed: Boolean = false, val meleePressed: Boolean = false)

/** UI Thread는 버튼을 기록하고 GL Thread는 프레임마다 읽습니다. 짧은 탭도 한 번 전달합니다. */
class DigitalInput {
    private val held = mutableSetOf<GameAction>()
    private var jump = false
    private var melee = false

    @Synchronized fun set(action: GameAction, pressed: Boolean) {
        if (pressed && held.add(action)) {
            if (action == GameAction.JUMP) jump = true
            if (action == GameAction.MELEE) melee = true
        } else if (!pressed) held.remove(action)
    }

    @Synchronized fun drain(): DigitalSnapshot {
        val axis = (if (GameAction.RIGHT in held) 1f else 0f) - (if (GameAction.LEFT in held) 1f else 0f)
        return DigitalSnapshot(axis, jump, melee).also { jump = false; melee = false }
    }

    @Synchronized fun clear() { held.clear(); jump = false; melee = false }
}
