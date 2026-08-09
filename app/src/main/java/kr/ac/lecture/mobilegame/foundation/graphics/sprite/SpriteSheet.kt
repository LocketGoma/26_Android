package kr.ac.lecture.mobilegame.foundation.graphics.sprite

import kr.ac.lecture.mobilegame.foundation.graphics.Texture

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
        )
    }

    fun row(row: Int): List<SpriteRegion> = (0 until columns).map { region(it, row) }
}
