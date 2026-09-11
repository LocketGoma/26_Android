package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.collision.AABBCollisionComponent
import kr.ac.lecture.mobilegame.foundation.component.MovementComponent
import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion

enum class ItemType { LIFE, BOMB }

class ItemPickup(
    x: Float,
    y: Float,
    val type: ItemType,
    private val region: SpriteRegion?,
) : GameObject(Vec2(x, y), Vec2(0.14f, 0.07f)) {
    init {
        collision = AABBCollisionComponent(transform, size.copy())
        movement = MovementComponent(transform, maxSpeed = 0.24f).apply {
            setMoveDirection(0f, -1f)
            velocity.y = -0.24f
        }
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        transform.rotation += 70f * deltaTime
        if (position.y < -1.12f) active = false
    }

    override fun draw(renderer: Renderer2D) {
        if (region != null) {
            renderer.drawSprite(region, position, renderer.aspectCorrectedSize(region, 0.14f),
                rotation = transform.rotation)
        } else {
            renderer.drawRect(position, size,
                if (type == ItemType.LIFE) Color.CYAN else Color.YELLOW, transform.rotation)
        }
    }
}
