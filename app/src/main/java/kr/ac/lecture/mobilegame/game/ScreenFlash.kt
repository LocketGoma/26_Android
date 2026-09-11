package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/** 피격과 폭탄 사용 순간을 짧은 전체 화면 색 변화로 분명하게 보여 줍니다. */
class ScreenFlash(
    private val color: Color,
    private val durationSeconds: Float,
) : GameObject(Vec2(), Vec2(2f, 2f)) {
    private var elapsed = 0f

    override fun update(deltaTime: Float) {
        elapsed += deltaTime
        if (elapsed >= durationSeconds) active = false
    }

    override fun draw(renderer: Renderer2D) {
        val alpha = (1f - elapsed / durationSeconds).coerceIn(0f, 1f) * color.alpha
        renderer.drawRect(position, size,
            Color(color.red * alpha, color.green * alpha, color.blue * alpha, alpha))
    }
}
