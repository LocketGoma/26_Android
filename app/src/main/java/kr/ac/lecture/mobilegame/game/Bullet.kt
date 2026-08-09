package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion

/** 학생 수정 영역: 탄속, 크기, 관통 여부를 실험할 수 있습니다. */
class Bullet(
    x: Float,
    y: Float,
    private val sprite: SpriteRegion? = null,
) : GameObject(Vec2(x, y), Vec2(0.05f, 0.13f)) {
    private val speed = GameConfig.BULLET_SPEED
    override fun update(deltaTime: Float) {
        position.y += speed * deltaTime
        if (position.y > 1.1f) active = false
    }
    override fun draw(renderer: Renderer2D) {
        sprite?.let { renderer.drawSprite(it, position, size) }
            ?: renderer.drawRect(position, size, Color.YELLOW)
    }
}
