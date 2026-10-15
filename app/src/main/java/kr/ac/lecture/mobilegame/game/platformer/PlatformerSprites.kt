package kr.ac.lecture.mobilegame.game.platformer

import kr.ac.lecture.mobilegame.R
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteSheet

data class FrameArt(val region: SpriteRegion, val feet: Float)

/** PNG를 읽고 동작별 그림과 발 위치를 묶습니다. 이동·충돌 계산은 여기에 넣지 않습니다. */
class PlatformerSprites(private val resources: ResourceManager) {
    private fun frames(id: Int, feet: List<Float>): List<FrameArt> {
        val sheet = SpriteSheet(resources.texture(id), 2, 2)
        return (0..3).map { FrameArt(sheet.region(it % 2, it / 2), feet[it]) }
    }
    val idle = frames(R.drawable.platformer_player_idle, listOf(0.92344f, 0.91866f, 0.84370f, 0.85486f))
    val run = frames(R.drawable.platformer_player_run, listOf(0.91866f, 0.91228f, 0.84370f, 0.84370f))
    val jump = frames(R.drawable.platformer_player_jump, listOf(0.92344f, 0.94099f, 0.72408f, 0.80861f))
    val hurt = frames(R.drawable.platformer_player_hurt, listOf(0.95853f, 0.96332f, 0.87241f, 0.87241f))
    val death = frames(R.drawable.platformer_player_death, listOf(0.98724f, 0.99841f, 0.85805f, 0.87241f))
    val cat = frames(R.drawable.platformer_enemy_cat_walk, listOf(0.91228f, 0.92026f, 0.84689f, 0.84689f))
    val snake = frames(R.drawable.platformer_enemy_snake_slither, listOf(0.79955f, 0.79955f, 0.79684f, 0.80361f))
    val background = SpriteSheet(resources.texture(R.drawable.platformer_background_grass), 1, 1).region(0, 0)
    private val items = SpriteSheet(resources.texture(R.drawable.platformer_items), 2, 1)
    val apple = crop(items.region(0, 0), 0.25592f, 0.15220f, 0.86246f, 0.86584f)
    val acorn = crop(items.region(1, 0), 0.20857f, 0.20068f, 0.78918f, 0.88952f)
    private val platforms = SpriteSheet(resources.texture(R.drawable.platformer_platforms), 1, 2)
    // 발판 그림의 투명 여백을 빼서 윗면과 충돌 상단을 같은 위치에 그립니다.
    val ground = crop(platforms.region(0, 0), 0.06220f, 0.52313f, 0.93780f, 0.85646f)
    val wood = crop(platforms.region(0, 1), 0.06220f, 0.25997f, 0.93780f, 0.53748f)

    private fun crop(source: SpriteRegion, left: Float, top: Float, right: Float, bottom: Float): SpriteRegion {
        val width = source.uRight - source.uLeft
        val height = source.vBottom - source.vTop
        return source.copy(uLeft = source.uLeft + left * width, uRight = source.uLeft + right * width,
            vTop = source.vTop + top * height, vBottom = source.vTop + bottom * height,
            widthPixels = source.widthPixels * (right - left), heightPixels = source.heightPixels * (bottom - top))
    }
}
