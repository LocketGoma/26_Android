package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.game.sprite.UiSprites
import kr.ac.lecture.mobilegame.foundation.ui.Hud

/** 기존 UI Sprite Sheet만 사용해 플레이 중 숫자와 아이콘을 그립니다. */
class GameHud(private val sprites: UiSprites?) : Hud<GameSession> {
    private var topOffset = 0f

    fun setTopInset(pixels: Int, viewportHeight: Int) {
        require(viewportHeight > 0)
        // OpenGL의 세로 범위는 -1~1입니다. 픽셀 여백을 같은 비율로 변환합니다.
        topOffset = pixels.coerceAtLeast(0) * 2f / viewportHeight
    }

    private fun topY(y: Float) = y - topOffset

    override fun draw(renderer: Renderer2D, state: GameSession) {
        val session = state
        if (!session.isPlaying && session.state != GameState.GAME_OVER) return
        val ui = sprites ?: return drawFallback(renderer, session)

        renderer.drawSprite(ui.healthLabel, Vec2(-0.83f, topY(0.92f)), Vec2(0.25f, 0.075f))
        repeat(GameConfig.PLAYER_MAX_LIFE) { index ->
            val icon = if (index < session.life) ui.healthFull else ui.healthEmpty
            renderer.drawSprite(icon, Vec2(-0.52f + index * 0.13f, topY(0.92f)), Vec2(0.105f, 0.105f))
        }

        renderer.drawSprite(ui.bombLabel, Vec2(0.56f, topY(0.92f)), Vec2(0.22f, 0.075f))
        renderer.drawSprite(if (session.bombs > 0) ui.bombFull else ui.bombEmpty, Vec2(0.74f, topY(0.92f)), Vec2(0.11f, 0.11f))
        renderer.drawSprite(ui.multiply, Vec2(0.85f, topY(0.92f)), Vec2(0.075f, 0.075f))
        renderer.drawSprite(ui.digit(session.bombs), Vec2(0.94f, topY(0.92f)), Vec2(0.075f, 0.095f))

        renderer.drawSprite(ui.scoreLabel, Vec2(-0.82f, topY(0.79f)), Vec2(0.23f, 0.07f))
        drawNumber(renderer, ui, session.score, startX = -0.64f, y = topY(0.79f), maxDigits = 7)

        renderer.drawSprite(ui.multiply, Vec2(0.24f, topY(0.79f)), Vec2(0.07f, 0.07f))
        drawNumber(renderer, ui, session.cycle, startX = 0.31f, y = topY(0.79f), maxDigits = 3)

        if (session.state == GameState.NORMAL) {
            val tens = session.timerSeconds / 10
            val ones = session.timerSeconds % 10
            val tensSprite = ui.digit(tens)
            val onesSprite = ui.digit(ones)
            renderer.drawRect(Vec2(0f, topY(0.91f)), Vec2(0.31f, 0.13f), Color(0.02f, 0.03f, 0.07f, 0.88f))
            renderer.drawSprite(tensSprite, Vec2(-0.07f, topY(0.91f)), renderer.aspectCorrectedSize(tensSprite, TIMER_DIGIT_WIDTH))
            renderer.drawSprite(onesSprite, Vec2(0.07f, topY(0.91f)), renderer.aspectCorrectedSize(onesSprite, TIMER_DIGIT_WIDTH))
        }
    }

    private fun drawNumber(
        renderer: Renderer2D,
        ui: UiSprites,
        value: Int,
        startX: Float,
        y: Float,
        maxDigits: Int,
    ) {
        value.coerceAtLeast(0).toString().takeLast(maxDigits).forEachIndexed { index, character ->
            renderer.drawSprite(ui.digit(character.digitToInt()), Vec2(startX + index * 0.065f, y), Vec2(0.055f, 0.075f))
        }
    }

    fun drawBossHp(renderer: Renderer2D, hp: Int?, maxHp: Int?) {
        if (hp == null || maxHp == null || maxHp <= 0) return
        renderer.drawRect(Vec2(0f, topY(0.69f)), Vec2(1.5f, 0.055f), Color(0.15f, 0.15f, 0.20f, 0.9f))
        val ratio = (hp.toFloat() / maxHp).coerceIn(0f, 1f)
        if (ratio > 0f) renderer.drawRect(Vec2(-0.75f + 0.75f * ratio, topY(0.69f)), Vec2(1.5f * ratio, 0.035f), Color.RED)
    }

    private fun drawFallback(renderer: Renderer2D, session: GameSession) {
        val lifeRatio = session.life / GameConfig.PLAYER_MAX_LIFE.toFloat()
        renderer.drawRect(Vec2(-0.9f + 0.3f * lifeRatio, topY(0.93f)), Vec2(0.6f * lifeRatio, 0.035f), Color.CYAN)
    }

    companion object {
        private const val TIMER_DIGIT_WIDTH = 0.12f
    }
}

data class GameUiSnapshot(
    val state: GameState,
    val score: Int,
    val bombs: Int,
    val highScores: List<Int>,
    val showCollision: Boolean,
)

fun interface GameUiListener {
    fun onChanged(snapshot: GameUiSnapshot)
}
