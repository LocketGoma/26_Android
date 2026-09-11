package kr.ac.lecture.mobilegame.game

import org.junit.Assert.assertEquals
import org.junit.Test

class GameConfigTest {
    @Test fun enemyRateGrowsByTwentyPercentEachCycle() {
        assertEquals(1f, GameConfig.enemyMultiplier(1), 0.0001f)
        assertEquals(1.2f, GameConfig.enemyMultiplier(2), 0.0001f)
        assertEquals(1.44f, GameConfig.enemyMultiplier(3), 0.0001f)
    }

    @Test fun bossPhaseNormalEnemyRateIsTenPercent() {
        assertEquals(GameConfig.normalSpawnInterval(4) * 10f,
            GameConfig.bossSpawnInterval(4), 0.0001f)
    }

    @Test fun bossHpUsesCycleFormula() {
        assertEquals(110, GameConfig.bossMaxHp(1))
        assertEquals(120, GameConfig.bossMaxHp(2))
        assertEquals(150, GameConfig.bossMaxHp(5))
    }
}
