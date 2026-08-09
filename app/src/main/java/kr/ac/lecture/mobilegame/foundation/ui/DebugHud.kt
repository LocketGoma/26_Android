package kr.ac.lecture.mobilegame.foundation.ui

import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/** 글꼴 시스템 전에는 점수를 상단 막대 길이, GameOver를 붉은 오버레이로 시각화합니다. */
class DebugHud : Hud {
    private var score = 0
    private var gameOver = false
    override fun updateScore(score: Int) { this.score = score }
    override fun showGameOver(visible: Boolean) { gameOver = visible }
    override fun draw(renderer: Renderer2D) {
        renderer.drawRect(Vec2(-0.95f + (score.coerceAtMost(100) / 100f), 0.94f), Vec2((score.coerceAtMost(100) / 50f).coerceAtLeast(0.02f), 0.025f), Color.YELLOW)
        if (gameOver) renderer.drawRect(Vec2(0f, 0f), Vec2(1.8f, 0.18f), Color(0.8f, 0.05f, 0.08f, 0.8f))
    }
}
