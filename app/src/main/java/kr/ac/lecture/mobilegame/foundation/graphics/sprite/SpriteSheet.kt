package kr.ac.lecture.mobilegame.foundation.graphics.sprite

import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.graphics.Texture

/**
 * Texture에서 그림 하나가 사용할 영역입니다. UV는 그림 전체를 0..1로 나타낸 좌표입니다.
 * 영역만 나누므로 원본 그림을 여러 장으로 복사하지 않습니다.
 */
data class SpriteRegion(
    val texture: Texture,
    val uLeft: Float,
    val vTop: Float,
    val uRight: Float,
    val vBottom: Float,
    // UV의 Half-Texel Inset과 원본 셀 크기는 서로 다른 정보입니다.
    val widthPixels: Float = texture.width * (uRight - uLeft),
    val heightPixels: Float = texture.height * (vBottom - vTop),
) {
    /** 셀 안의 0..1 범위를 잘라냅니다. 투명 여백을 제외해도 원래 Texture는 공유합니다. */
    fun crop(left: Float, top: Float, right: Float, bottom: Float): SpriteRegion {
        require(left >= 0f && top >= 0f && right <= 1f && bottom <= 1f && left < right && top < bottom)
        val width = uRight - uLeft
        val height = vBottom - vTop
        return copy(uLeft = uLeft + left * width, uRight = uLeft + right * width, vTop = vTop + top * height, vBottom = vTop + bottom * height, widthPixels = widthPixels * (right - left), heightPixels = heightPixels * (bottom - top))
    }
}

/**
 * [강사 제공 기반 시스템]
 * 동일한 크기의 셀이 격자로 배치된 Texture를 UV 영역으로 나눕니다.
 * 실제 Bitmap을 여러 장으로 잘라 복사하지 않으므로 GPU Texture는 한 장만 사용합니다.
 */
class SpriteSheet(
    val texture: Texture,
    val columns: Int,
    val rows: Int,
) {
    init {
        require(columns > 0) { "columns must be greater than zero" }
        require(rows > 0) { "rows must be greater than zero" }
    }

    val frameCount: Int = columns * rows

    fun region(column: Int, row: Int, insetHalfTexel: Boolean = true): SpriteRegion {
        require(column in 0 until columns) { "column=$column, valid=0..${columns - 1}" }
        require(row in 0 until rows) { "row=$row, valid=0..${rows - 1}" }

        val cellU = 1f / columns
        val cellV = 1f / rows
        val insetU = if (insetHalfTexel) 0.5f / texture.width else 0f
        val insetV = if (insetHalfTexel) 0.5f / texture.height else 0f
        return SpriteRegion(texture = texture, uLeft = column * cellU + insetU, vTop = row * cellV + insetV, uRight = (column + 1) * cellU - insetU, vBottom = (row + 1) * cellV - insetV, widthPixels = texture.width.toFloat() / columns, heightPixels = texture.height.toFloat() / rows)
    }

    fun row(row: Int): List<SpriteRegion> = (0 until columns).map { region(it, row) }
}

/**
 * ResourceManager가 읽은 Texture에 격자 정보를 붙입니다.
 * 같은 그림을 다시 요청하면 ResourceManager의 캐시를 사용합니다.
 */
class SpriteSheetLoader(private val resources: ResourceManager) {
    fun load(resourceId: Int, columns: Int, rows: Int): SpriteSheet = SpriteSheet(resources.texture(resourceId), columns, rows)
}
