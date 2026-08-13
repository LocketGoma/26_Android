package kr.ac.lecture.mobilegame.game.sprite

import kr.ac.lecture.mobilegame.R
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteSheetLoader

data class ItemSprites(
    val healthPickup: SpriteRegion,
    val bombPickup: SpriteRegion,
)

/**
 * [학생 실습/수정 영역 + 제공 샘플]
 * 필드에 놓이는 체력/폭탄 아이템을 2×1 Sprite Sheet의 셀에 연결합니다.
 *
 * 두 아이콘은 애니메이션이 없는 정적 이미지이므로 SpriteClip으로 묶지 않습니다.
 * 학생은 아이템 충돌 결과와 획득 효과를 설명할 수 있는 범위에서 이 매핑을 수정합니다.
 */
object ItemSpriteCatalog {
    fun load(resources: ResourceManager): ItemSprites {
        val sheet = SpriteSheetLoader(resources).load(
            R.drawable.item_sprites,
            columns = 2,
            rows = 1,
        )

        return ItemSprites(
            healthPickup = sheet.region(column = 0, row = 0),
            bombPickup = sheet.region(column = 1, row = 0),
        )
    }
}
