package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
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
    init {
        this.sprite = SpriteComponent(this, useObjectSize = true).apply {
            sprite?.let { addSprite(it.toAsset("Idle")) }
        }
        collision = AABBCollisionComponent(transform, size.copy())
        movement = MovementComponent(transform, maxSpeed = speed).apply {
            setMoveDirection(0f, -1f)
            velocity.y = -speed // 기존 샘플과 같은 등속 이동으로 시작합니다.
        }
    }
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        if (position.y < -1.15f) active = false
    }
    override fun draw(renderer: Renderer2D) {
        if (sprite?.getCurrentSprite() != null) super.draw(renderer)
        else renderer.drawRect(position, Vec2(size.x * transform.scale.x, size.y * transform.scale.y),
            Color.RED, transform.rotation)
    }
}
