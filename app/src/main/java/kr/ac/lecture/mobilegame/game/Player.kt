package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot
import kr.ac.lecture.mobilegame.game.sprite.SpriteClip
import kr.ac.lecture.mobilegame.foundation.component.SpriteComponent
import kr.ac.lecture.mobilegame.foundation.collision.AABBCollisionComponent

/** 학생 수정 영역: 이동 규칙, 속도, 외형을 바꿔 보세요. */
class Player(sprite: SpriteClip? = null) : GameObject(Vec2(0f, -0.72f), Vec2(0.28f, 0.22f)) {
    init {
        this.sprite = SpriteComponent(this, useObjectSize = true).apply {
            sprite?.let { addSprite(it.toAsset("Idle")) }
        }
        collision = AABBCollisionComponent(transform, size.copy())
    }
    private var target = Vec2(position.x, position.y)
    private val speed = GameConfig.PLAYER_SPEED // 초당 월드 좌표 이동량

    fun readInput(input: InputSnapshot) {
        if (input.touching) target = Vec2(input.worldX, input.worldY)
        // TODO(센서 실습): accelerometerX를 target 또는 velocity로 변환해 보세요.
    }

    fun reset() {
        position.x = 0f
        position.y = -0.72f
        target = Vec2(position.x, position.y)
        active = true
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        // 기존 터치 추종 규칙을 유지합니다. MovementComponent로 교체하는 것은 학생 실습입니다.
        val dx = target.x - position.x
        val dy = target.y - position.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        if (distance > 0.001f) {
            val step = (speed * deltaTime).coerceAtMost(distance)
            position.x += dx / distance * step
            position.y += dy / distance * step
        }
        position.x = position.x.coerceIn(-0.9f, 0.9f)
        position.y = position.y.coerceIn(-0.9f, 0.9f)
    }

    override fun draw(renderer: Renderer2D) {
        if (sprite?.getCurrentSprite() != null) super.draw(renderer)
        else renderer.drawRect(position, Vec2(size.x * transform.scale.x, size.y * transform.scale.y),
            Color.CYAN, transform.rotation)
    }
}
