package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteAsset
import kr.ac.lecture.mobilegame.foundation.component.SpriteComponent
import kr.ac.lecture.mobilegame.foundation.component.MovementComponent
import kr.ac.lecture.mobilegame.foundation.collision.AABBCollisionComponent

/** 학생 수정 영역: 탄속, 크기, 관통 여부를 실험할 수 있습니다. */
class Bullet(
    x: Float,
    y: Float,
    sprite: SpriteRegion? = null,
    val owner: BulletOwner = BulletOwner.PLAYER,
) : GameObject(Vec2(x, y), Vec2(0.05f, 0.13f)) {
    init {
        this.sprite = SpriteComponent(this, useObjectSize = true).apply {
            sprite?.let { addSprite(SpriteAsset("Bullet", listOf(it),
                it.widthPixels, it.heightPixels)) }
        }
        collision = AABBCollisionComponent(transform, size.copy())
        val speed = if (owner == BulletOwner.PLAYER) GameConfig.BULLET_SPEED else GameConfig.ENEMY_BULLET_SPEED
        val direction = if (owner == BulletOwner.PLAYER) 1f else -1f
        movement = MovementComponent(transform, maxSpeed = speed).apply {
            setMoveDirection(0f, direction)
            velocity.y = speed * direction
        }
    }
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        if (position.y > 1.1f || position.y < -1.1f) active = false
    }
    override fun draw(renderer: Renderer2D) {
        if (sprite?.getCurrentSprite() != null) super.draw(renderer)
        else renderer.drawRect(position, Vec2(size.x * transform.scale.x, size.y * transform.scale.y),
            if (owner == BulletOwner.PLAYER) Color.YELLOW else Color.RED, transform.rotation)
    }
}
