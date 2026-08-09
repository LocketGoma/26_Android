package kr.ac.lecture.mobilegame.game.sprite

import kr.ac.lecture.mobilegame.R
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteSheetLoader

data class ShootingSprites(
    val playerIdleFrames: List<SpriteRegion>,
    val playerBankLeftFrames: List<SpriteRegion>,
    val playerBankRightFrames: List<SpriteRegion>,
    val playerStates: List<SpriteRegion>,
    val playerHitFrames: List<SpriteRegion>,
    val playerDestroyedFrames: List<SpriteRegion>,
    val smallEnemyFrames: List<SpriteRegion>,
    val bomberEnemyFrames: List<SpriteRegion>,
    val smallEnemyHitFrames: List<SpriteRegion>,
    val smallEnemyDestroyedFrames: List<SpriteRegion>,
    val bomberEnemyHitFrames: List<SpriteRegion>,
    val bomberEnemyDestroyedFrames: List<SpriteRegion>,
    val playerBullet: SpriteRegion,
    val enemyBullet: SpriteRegion,
    val impact: List<SpriteRegion>,
    val explosion: List<SpriteRegion>,
) {
    fun newPlayerIdleClip() = SpriteClip(playerIdleFrames, secondsPerFrame = 0.14f)
    fun newPlayerBankLeftClip() = SpriteClip(playerBankLeftFrames, secondsPerFrame = 0.12f)
    fun newPlayerBankRightClip() = SpriteClip(playerBankRightFrames, secondsPerFrame = 0.12f)
    fun newPlayerHitClip() = SpriteClip(playerHitFrames, secondsPerFrame = 0.08f, looping = false)
    fun newPlayerDestroyedClip() = SpriteClip(playerDestroyedFrames, secondsPerFrame = 0.10f, looping = false)
    fun newSmallEnemyClip() = SpriteClip(smallEnemyFrames, secondsPerFrame = 0.16f)
    fun newBomberEnemyClip() = SpriteClip(bomberEnemyFrames, secondsPerFrame = 0.20f)
    fun newSmallEnemyHitClip() = SpriteClip(smallEnemyHitFrames, secondsPerFrame = 0.07f, looping = false)
    fun newSmallEnemyDestroyedClip() = SpriteClip(smallEnemyDestroyedFrames, secondsPerFrame = 0.09f, looping = false)
    fun newBomberEnemyHitClip() = SpriteClip(bomberEnemyHitFrames, secondsPerFrame = 0.07f, looping = false)
    fun newBomberEnemyDestroyedClip() = SpriteClip(bomberEnemyDestroyedFrames, secondsPerFrame = 0.11f, looping = false)
}

/**
 * [학생 실습/수정 영역 + 제공 샘플]
 * “0행은 Player idle” 같은 게임 의미를 셀 좌표에 붙이는 곳입니다.
 * 학생은 이미지 교체, 행/열 매핑, 재생 속도와 상태 조합을 수정합니다.
 */
object ShootingSpriteCatalog {
    fun load(resources: ResourceManager): ShootingSprites {
        val loader = SpriteSheetLoader(resources)
        val player = loader.load(R.drawable.player_sprites, columns = 4, rows = 4)
        val effects = loader.load(R.drawable.effects_sprites, columns = 4, rows = 4)
        val enemies = loader.load(R.drawable.enemy_sprites, columns = 4, rows = 2)
        val playerDamage = loader.load(R.drawable.player_damage_sprites, columns = 4, rows = 2)
        val enemyDamage = loader.load(R.drawable.enemy_damage_sprites, columns = 4, rows = 4)

        return ShootingSprites(
            playerIdleFrames = player.row(0),
            playerBankLeftFrames = player.row(1),
            playerBankRightFrames = player.row(2),
            playerStates = player.row(3),
            playerHitFrames = playerDamage.row(0),
            playerDestroyedFrames = playerDamage.row(1),
            smallEnemyFrames = enemies.row(0),
            bomberEnemyFrames = enemies.row(1),
            smallEnemyHitFrames = enemyDamage.row(0),
            smallEnemyDestroyedFrames = enemyDamage.row(1),
            bomberEnemyHitFrames = enemyDamage.row(2),
            bomberEnemyDestroyedFrames = enemyDamage.row(3),
            playerBullet = effects.region(column = 0, row = 0),
            enemyBullet = effects.region(column = 0, row = 1),
            impact = effects.row(2),
            explosion = effects.row(3),
        )
    }
}
