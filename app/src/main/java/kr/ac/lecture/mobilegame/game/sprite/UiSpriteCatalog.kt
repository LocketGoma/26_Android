package kr.ac.lecture.mobilegame.game.sprite

import kr.ac.lecture.mobilegame.R
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteSheetLoader

data class UiSprites(
    val healthFull: SpriteRegion,
    val healthEmpty: SpriteRegion,
    val bombFull: SpriteRegion,
    val bombEmpty: SpriteRegion,
    val multiply: SpriteRegion,
    val digits: List<SpriteRegion>,
    val healthLabel: SpriteRegion,
    val bombLabel: SpriteRegion,
    val scoreLabel: SpriteRegion,
    val plus: SpriteRegion,
    val minus: SpriteRegion,
) {
    fun digit(value: Int): SpriteRegion {
        require(value in 0..9) { "digit must be in 0..9: $value" }
        return digits[value]
    }
}

/** [학생 실습/수정 영역 + 제공 샘플] UI 의미를 5×4 Sprite Sheet의 셀에 연결합니다. */
object UiSpriteCatalog {
    fun load(resources: ResourceManager): UiSprites {
        val sheet = SpriteSheetLoader(resources).load(
            R.drawable.ui_sprites,
            columns = 5,
            rows = 4,
        )
        val digits = sheet.row(1) + sheet.row(2)

        return UiSprites(
            healthFull = sheet.region(column = 0, row = 0),
            healthEmpty = sheet.region(column = 1, row = 0),
            bombFull = sheet.region(column = 2, row = 0),
            bombEmpty = sheet.region(column = 3, row = 0),
            multiply = sheet.region(column = 4, row = 0),
            digits = digits,
            healthLabel = sheet.region(column = 0, row = 3),
            bombLabel = sheet.region(column = 1, row = 3),
            scoreLabel = sheet.region(column = 2, row = 3),
            plus = sheet.region(column = 3, row = 3),
            minus = sheet.region(column = 4, row = 3),
        )
    }
}
