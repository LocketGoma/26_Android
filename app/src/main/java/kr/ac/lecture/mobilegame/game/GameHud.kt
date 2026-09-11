package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.game.sprite.UiSprites

/** 기존 UI Sprite Sheet만 사용해 플레이 중 숫자와 아이콘을 그립니다. */
class GameHud(private val sprites: UiSprites?) {
    fun draw(
        renderer: Renderer2D,
        session: GameSession,
        bossHp: Int?,
        bossMaxHp: Int?,
    ) {
        if (!session.isPlaying && session.state != GameState.GAME_OVER) return
        val ui = sprites ?: return drawFallback(renderer, session, bossHp, bossMaxHp)

        renderer.drawSprite(ui.healthLabel, Vec2(-0.83f, 0.92f), Vec2(0.25f, 0.075f))
        repeat(GameConfig.PLAYER_MAX_LIFE) { index ->
            val icon = if (index < session.life) ui.healthFull else ui.healthEmpty
            renderer.drawSprite(icon, Vec2(-0.52f + index * 0.13f, 0.92f), Vec2(0.105f, 0.105f))
        }

        renderer.drawSprite(ui.bombLabel, Vec2(0.56f, 0.92f), Vec2(0.22f, 0.075f))
        renderer.drawSprite(if (session.bombs > 0) ui.bombFull else ui.bombEmpty,
            Vec2(0.74f, 0.92f), Vec2(0.11f, 0.11f))
        renderer.drawSprite(ui.multiply, Vec2(0.85f, 0.92f), Vec2(0.075f, 0.075f))
        renderer.drawSprite(ui.digit(session.bombs), Vec2(0.94f, 0.92f), Vec2(0.075f, 0.095f))

        renderer.drawSprite(ui.scoreLabel, Vec2(-0.82f, 0.79f), Vec2(0.23f, 0.07f))
        drawNumber(renderer, ui, session.score, startX = -0.64f, y = 0.79f, maxDigits = 7)

        renderer.drawSprite(ui.multiply, Vec2(0.24f, 0.79f), Vec2(0.07f, 0.07f))
        drawNumber(renderer, ui, session.cycle, startX = 0.31f, y = 0.79f, maxDigits = 3)

        if (session.state == GameState.NORMAL) {
            val tens = session.timerSeconds / 10
            val ones = session.timerSeconds % 10
            val tensSprite = ui.digit(tens)
            val onesSprite = ui.digit(ones)
            renderer.drawRect(Vec2(0f, 0.91f), Vec2(0.31f, 0.13f), Color(0.02f, 0.03f, 0.07f, 0.88f))
            renderer.drawSprite(tensSprite, Vec2(-0.07f, 0.91f),
                renderer.aspectCorrectedSize(tensSprite, TIMER_DIGIT_WIDTH))
            renderer.drawSprite(onesSprite, Vec2(0.07f, 0.91f),
                renderer.aspectCorrectedSize(onesSprite, TIMER_DIGIT_WIDTH))
        }

        drawBossHp(renderer, bossHp, bossMaxHp)
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
            renderer.drawSprite(ui.digit(character.digitToInt()), Vec2(startX + index * 0.065f, y),
                Vec2(0.055f, 0.075f))
        }
    }

    private fun drawBossHp(renderer: Renderer2D, hp: Int?, maxHp: Int?) {
        if (hp == null || maxHp == null || maxHp <= 0) return
        renderer.drawRect(Vec2(0f, 0.69f), Vec2(1.5f, 0.055f), Color(0.15f, 0.15f, 0.20f, 0.9f))
        val ratio = (hp.toFloat() / maxHp).coerceIn(0f, 1f)
        if (ratio > 0f) renderer.drawRect(Vec2(-0.75f + 0.75f * ratio, 0.69f),
            Vec2(1.5f * ratio, 0.035f), Color.RED)
    }

    private fun drawFallback(renderer: Renderer2D, session: GameSession, hp: Int?, maxHp: Int?) {
        val lifeRatio = session.life / GameConfig.PLAYER_MAX_LIFE.toFloat()
        renderer.drawRect(Vec2(-0.9f + 0.3f * lifeRatio, 0.93f), Vec2(0.6f * lifeRatio, 0.035f), Color.CYAN)
        drawBossHp(renderer, hp, maxHp)
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
)

fun interface GameUiListener {
    fun onChanged(snapshot: GameUiSnapshot)
}
