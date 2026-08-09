package kr.ac.lecture.mobilegame.foundation.graphics.sprite

import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager

/** [강사 제공 기반 시스템] Texture 캐시와 SpriteSheet 생성을 연결합니다. */
class SpriteSheetLoader(private val resources: ResourceManager) {
    fun load(resourceId: Int, columns: Int, rows: Int): SpriteSheet =
        SpriteSheet(resources.texture(resourceId), columns, rows)
}
