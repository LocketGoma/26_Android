package kr.ac.lecture.mobilegame.foundation.ui

import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/** main의 간단한 표시 예제입니다. 점수는 위쪽 막대로, 게임 종료는 붉은 사각형으로 그립니다. */
class DebugHud : Hud {
    private var score = 0
    private var gameOver = false
    override fun updateScore(score: Int) { this.score = score }
    override fun showGameOver(visible: Boolean) { gameOver = visible }
    override fun draw(renderer: Renderer2D) {
        val scoreRatio = score.coerceIn(0, 100) / 100f
        renderer.drawRect(
            center = Vec2(-0.95f + scoreRatio, 0.94f),
            size = Vec2((scoreRatio * 2f).coerceAtLeast(0.02f), 0.025f),
            color = Color.YELLOW,
        )
        if (gameOver) {
            renderer.drawRect(Vec2(), Vec2(1.8f, 0.18f), Color(0.8f, 0.05f, 0.08f, 0.8f))
        }
    }
}
