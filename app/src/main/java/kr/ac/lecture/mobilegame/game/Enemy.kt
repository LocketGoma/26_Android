package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.game.sprite.SpriteClip
import kr.ac.lecture.mobilegame.foundation.component.SpriteComponent
import kr.ac.lecture.mobilegame.foundation.component.MovementComponent
import kr.ac.lecture.mobilegame.foundation.collision.AABBCollisionComponent

/** 학생 수정 영역: 적 이동 패턴과 난이도에 따라 speed를 확장하세요. */
class Enemy(
    x: Float,
    speed: Float = 0.45f,
    sprite: SpriteClip? = null,
) : GameObject(Vec2(x, 1.05f), Vec2(0.27f, 0.22f)) {
    override val fallbackColor = Color.RED
    init {
        this.sprite = SpriteComponent(this, useObjectSize = true).apply {
            sprite?.let { addSprite(it.toAsset("Idle")) }
        }
        collision = AABBCollisionComponent(transform, size.copy())
        movement = MovementComponent.constantVelocity(transform, 0f, -speed)
    }
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        if (position.y < -1.15f) active = false
    }
}
