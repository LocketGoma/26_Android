package kr.ac.lecture.mobilegame.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSessionTest {
    @Test fun newGameStartsWithRequestedLifeAndBombs() {
        val session = GameSession()
        session.startGame()

        assertEquals(GameState.NORMAL, session.state)
        assertEquals(1, session.cycle)
        assertEquals(3, session.life)
        assertEquals(2, session.bombs)
        assertEquals(99, session.timerSeconds)
    }

    @Test fun normalPhaseShowsZeroThenChangesToBossAtOneHundredSeconds() {
        val session = GameSession().apply { startGame() }

        assertFalse(session.update(99.1f))
        assertEquals(0, session.timerSeconds)
        assertTrue(session.update(0.9f))
        assertEquals(GameState.BOSS, session.state)
    }

    @Test fun bossDeathStartsNextCycle() {
        val session = GameSession().apply {
            startGame()
            update(GameConfig.NORMAL_PHASE_SECONDS)
        }

        session.finishBoss()

        assertEquals(GameState.NORMAL, session.state)
        assertEquals(2, session.cycle)
        assertEquals(99, session.timerSeconds)
    }

    @Test fun hitResetsBombsAndFiveSecondInvincibilityBlocksDamage() {
        val session = GameSession().apply {
            startGame()
            repeat(3) { collectBomb() }
        }
        assertEquals(5, session.bombs)

        assertTrue(session.takeHit())
        assertEquals(2, session.life)
        assertEquals(2, session.bombs)
        assertFalse(session.takeHit())
        assertEquals(2, session.life)

        session.update(5f)
        assertTrue(session.takeHit())
        assertEquals(1, session.life)
    }

    @Test fun lifeAndBombPickupsRespectMaximums() {
        val session = GameSession()
        session.startGame()
        repeat(10) {
            session.collectLife()
            session.collectBomb()
        }

        assertEquals(3, session.life)
        assertEquals(5, session.bombs)
    }

    @Test fun bombUseStopsAtZero() {
        val session = GameSession().apply { startGame() }

        assertTrue(session.useBomb())
        assertTrue(session.useBomb())
        assertFalse(session.useBomb())
        assertEquals(0, session.bombs)
    }

    @Test fun thirdAcceptedHitEndsGame() {
        val session = GameSession().apply { startGame() }
        repeat(3) {
            assertTrue(session.takeHit())
            if (it < 2) session.update(GameConfig.INVINCIBLE_SECONDS)
        }

        assertEquals(0, session.life)
        assertEquals(GameState.GAME_OVER, session.state)
    }
}
