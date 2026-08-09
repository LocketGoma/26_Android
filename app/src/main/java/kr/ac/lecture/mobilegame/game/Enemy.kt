package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.game.sprite.SpriteClip

/** 학생 수정 영역: 적 이동 패턴과 난이도에 따라 speed를 확장하세요. */
class Enemy(
    x: Float,
    speed: Float = 0.45f,
    private val sprite: SpriteClip? = null,
) : GameObject(Vec2(x, 1.05f), Vec2(0.27f, 0.22f)) {
    private val moveSpeed = speed
    override fun update(deltaTime: Float) {
        sprite?.update(deltaTime)
        position.y -= moveSpeed * deltaTime
        if (position.y < -1.15f) active = false
    }
    override fun draw(renderer: Renderer2D) {
        sprite?.let { renderer.drawSprite(it.current, position, size) }
            ?: renderer.drawRect(position, size, Color.RED)
    }
}
