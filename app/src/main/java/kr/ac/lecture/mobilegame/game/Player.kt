package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot
import kr.ac.lecture.mobilegame.game.sprite.SpriteClip
import kr.ac.lecture.mobilegame.foundation.component.SpriteComponent
import kr.ac.lecture.mobilegame.foundation.collision.AABBCollisionComponent

/** 학생 수정 영역: 이동 규칙, 속도, 외형을 바꿔 보세요. */
open class Player(sprite: SpriteClip? = null) : GameObject(Vec2(0f, -0.72f), Vec2(0.28f, 0.22f)) {
    init {
        this.sprite = SpriteComponent(this, useObjectSize = true).apply {
            sprite?.let { addSprite(it.toAsset("Idle")) }
        }
        collision = AABBCollisionComponent(transform, size.copy())
    }

    override val fallbackColor = Color.CYAN
    protected val target = position.copy()
    protected val speed = GameConfig.PLAYER_SPEED // 초당 월드 좌표 이동량

    open fun readInput(input: InputSnapshot) {
        if (input.touching) {
            target.x = input.worldX.coerceIn(-0.9f, 0.9f)
            target.y = input.worldY.coerceIn(-0.9f, 0.9f)
        }
        // TODO(센서 실습): accelerometerX를 target 또는 velocity로 변환해 보세요.
    }

    fun reset() {
        position.x = 0f
        position.y = -0.72f
        target.x = position.x
        target.y = position.y
        sprite?.setSprite("Idle", restart = true)
        active = true
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        move(deltaTime)
        position.x = position.x.coerceIn(-0.9f, 0.9f)
        position.y = position.y.coerceIn(-0.9f, 0.9f)
    }

    /** 기본은 터치 목표 추종이며, 애드온은 이동 방식만 재정의할 수 있습니다. */
    protected open fun move(deltaTime: Float) {
        val dx = target.x - position.x
        val dy = target.y - position.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        if (distance > 0.001f) {
            val step = (speed * deltaTime).coerceAtMost(distance)
            position.x += dx / distance * step
            position.y += dy / distance * step
        }
    }

}
