package kr.ac.lecture.mobilegame.game

import kotlin.math.pow

/** 학생이 숫자만 바꾸며 게임 감각을 비교할 수 있도록 모아 둔 데이터 초안입니다. */
object GameConfig {
    const val PLAYER_SPEED = 2.4f
    const val BULLET_SPEED = 1.8f
    const val FIRE_INTERVAL_SECONDS = 0.28f
    const val ENEMY_BULLET_SPEED = 0.75f
    const val NORMAL_PHASE_SECONDS = 100f
    const val BASE_ENEMY_SPAWN_INTERVAL_SECONDS = 1f
    const val ENEMY_FIRE_INTERVAL_SECONDS = 1.8f
    const val BOSS_ENEMY_RATE = 0.1f
    const val INVINCIBLE_SECONDS = 5f
    const val PLAYER_MAX_LIFE = 3
    const val INITIAL_BOMB_COUNT = 2
    const val MAX_BOMB_COUNT = 5
    const val BOMB_DAMAGE = 10
    const val PLAYER_BULLET_DAMAGE = 10
    const val ITEM_DROP_CHANCE = 0.05f
    const val ENEMY_SCORE = 10
    const val BOSS_SCORE = 1_000

    fun enemyMultiplier(cycle: Int): Float =
        1.2f.pow((cycle.coerceAtLeast(1) - 1).toFloat())

    fun normalSpawnInterval(cycle: Int): Float =
        BASE_ENEMY_SPAWN_INTERVAL_SECONDS / enemyMultiplier(cycle)

    fun bossSpawnInterval(cycle: Int): Float =
        normalSpawnInterval(cycle) / BOSS_ENEMY_RATE

    fun bossMaxHp(cycle: Int): Int =
        (100f * (1f + cycle.coerceAtLeast(1) * 0.1f)).toInt()

    // TODO(데이터 실습): JSON 또는 Data Asset 성격의 외부 파일로 분리합니다.
}
