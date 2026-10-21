package kr.ac.lecture.mobilegame.foundation.collision

/**
 * AABB(Axis-Aligned Bounding Box)를 나타내는 data class입니다.
 * Y축 방향과 관계없이 minY <= maxY입니다. 두 상자의 경계만 닿으면 겹치지 않습니다.
 * OBB(회전 상자)는 회전축 투영이 필요하므로 선택 심화 과제로 남깁니다.
 */
data class Aabb(val left: Float, val minY: Float, val right: Float, val maxY: Float) {
    init {
        require(left <= right) { "left must be <= right" }
        require(minY <= maxY) { "minY must be <= maxY" }
    }

    fun overlaps(other: Aabb): Boolean = right > other.left && left < other.right && maxY > other.minY && minY < other.maxY

    companion object {
        fun fromCenter(x: Float, y: Float, width: Float, height: Float): Aabb {
            require(x.isFinite() && y.isFinite() && width.isFinite() && height.isFinite() && width >= 0f && height >= 0f)
            val halfWidth = width / 2f
            val halfHeight = height / 2f
            return Aabb(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight)
        }
    }
}
