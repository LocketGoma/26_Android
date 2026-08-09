package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion

/** 학생 수정 영역: 4프레임 효과를 재생한 뒤 스스로 비활성화하는 예제입니다. */
class Explosion(
    x: Float,
    y: Float,
    private val frames: List<SpriteRegion>,
) : GameObject(Vec2(x, y), Vec2(0.34f, 0.34f)) {
    private val secondsPerFrame = 0.08f
    private var elapsed = 0f

    override fun update(deltaTime: Float) {
        elapsed += deltaTime
        if (elapsed >= frames.size * secondsPerFrame) active = false
    }

    override fun draw(renderer: Renderer2D) {
        if (frames.isEmpty()) {
            renderer.drawRect(position, size, Color.YELLOW)
            return
        }
        val index = (elapsed / secondsPerFrame).toInt().coerceAtMost(frames.lastIndex)
        renderer.drawSprite(frames[index], position, size)
    }
}
