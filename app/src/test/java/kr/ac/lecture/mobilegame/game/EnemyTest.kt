package kr.ac.lecture.mobilegame.game

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnemyTest {
    @Test fun eliteNeedsFiveOnePointHits() {
        val elite = Enemy(x = 0f, kind = EnemyKind.ELITE, maxHp = GameConfig.ELITE_ENEMY_HP)

        repeat(4) { assertFalse(elite.damage(GameConfig.PLAYER_BULLET_DAMAGE)) }
        assertTrue(elite.active)
        assertTrue(elite.damage(GameConfig.PLAYER_BULLET_DAMAGE))
        assertFalse(elite.active)
    }
}
