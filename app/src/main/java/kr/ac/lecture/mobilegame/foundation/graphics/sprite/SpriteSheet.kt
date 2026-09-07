package kr.ac.lecture.mobilegame.foundation.graphics.sprite

import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.graphics.Texture

/**
 * 전체 Texture 중 Sprite 하나가 사용할 UV 범위를 담는 immutable data class입니다.
 * SpriteSheet가 계산해 반환하는 데이터이므로 같은 파일에서 계산식과 구조를 함께 확인합니다.
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
)

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
        return SpriteRegion(
            texture = texture,
            uLeft = column * cellU + insetU,
            vTop = row * cellV + insetV,
            uRight = (column + 1) * cellU - insetU,
            vBottom = (row + 1) * cellV - insetV,
            widthPixels = texture.width.toFloat() / columns,
            heightPixels = texture.height.toFloat() / rows,
        )
    }

    fun row(row: Int): List<SpriteRegion> = (0 until columns).map { region(it, row) }
}

/**
 * ResourceManager에서 Texture를 가져와 SpriteSheet를 생성하는 Loader입니다.
 * ResourceManager 참조만 보관하고 GPU 리소스를 직접 소유하지 않아 관련 타입과 한 파일에 둡니다.
 */
class SpriteSheetLoader(private val resources: ResourceManager) {
    fun load(resourceId: Int, columns: Int, rows: Int): SpriteSheet =
        SpriteSheet(resources.texture(resourceId), columns, rows)
}
