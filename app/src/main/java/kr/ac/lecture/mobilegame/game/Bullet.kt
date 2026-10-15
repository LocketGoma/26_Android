package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
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
) : GameObject(Vec2(x, y), Vec2(0.05f, 0.13f)) {
    override val fallbackColor = Color.YELLOW
    init {
        this.sprite = SpriteComponent(this, useObjectSize = true).apply {
            sprite?.let { addSprite(SpriteAsset("Bullet", listOf(it),
                it.widthPixels, it.heightPixels)) }
        }
        collision = AABBCollisionComponent(transform, size.copy())
        movement = MovementComponent.constantVelocity(transform, 0f, GameConfig.BULLET_SPEED)
    }
    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        if (position.y > 1.1f) active = false
    }
}
