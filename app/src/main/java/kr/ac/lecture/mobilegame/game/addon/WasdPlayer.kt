package kr.ac.lecture.mobilegame.game.addon

import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot
import kr.ac.lecture.mobilegame.game.Player
import kr.ac.lecture.mobilegame.game.sprite.SpriteClip
import kotlin.math.sqrt

/** 슈팅용 선택 애드온: 키 입력이 있을 때만 터치 목표 대신 이동 방향을 사용합니다. */
class WasdPlayer(sprite: SpriteClip? = null) : Player(sprite) {
    private val direction = Vec2()

    override fun readInput(input: InputSnapshot) {
        super.readInput(input)
        direction.x = input.horizontalAxis
        direction.y = input.verticalAxis
    }

    override fun move(deltaTime: Float) {
        val length = sqrt(direction.x * direction.x + direction.y * direction.y)
        if (length > 0f) {
            // 대각선 속도를 맞추고, 키를 놓으면 이전 터치 위치로 돌아가지 않게 합니다.
            target.x = position.x + direction.x / length * speed * deltaTime
            target.y = position.y + direction.y / length * speed * deltaTime
        }
        super.move(deltaTime)
    }
}
