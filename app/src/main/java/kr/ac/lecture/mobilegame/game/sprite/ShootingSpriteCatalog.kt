package kr.ac.lecture.mobilegame.game.sprite

import kr.ac.lecture.mobilegame.R
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteSheetLoader

data class ShootingSprites(
    val playerIdleFrames: List<SpriteRegion>,
    val playerHitFrames: List<SpriteRegion>,
    val playerDestroyedFrames: List<SpriteRegion>,
    val smallEnemyFrames: List<SpriteRegion>,
    val bomberEnemyFrames: List<SpriteRegion>,
    val playerBullet: SpriteRegion,
    val enemyBullet: SpriteRegion,
    val impact: List<SpriteRegion>,
    val explosion: List<SpriteRegion>,
) {
    fun newPlayerIdleClip() = SpriteClip(playerIdleFrames, secondsPerFrame = 0.14f)
    fun newSmallEnemyClip() = SpriteClip(smallEnemyFrames, secondsPerFrame = 0.16f)
    fun newBomberEnemyClip() = SpriteClip(bomberEnemyFrames, secondsPerFrame = 0.20f)
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

        return ShootingSprites(playerIdleFrames = player.row(0), playerHitFrames = playerDamage.row(0), playerDestroyedFrames = playerDamage.row(1), smallEnemyFrames = enemies.row(0), bomberEnemyFrames = enemies.row(1), playerBullet = effects.region(column = 0, row = 0), enemyBullet = effects.region(column = 0, row = 1), impact = effects.row(2), explosion = effects.row(3))
    }
}
