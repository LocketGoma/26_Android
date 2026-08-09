package kr.ac.lecture.mobilegame.foundation.collision

/**
 * 축에 정렬된 충돌 상자. 경계가 닿기만 한 경우는 겹치지 않은 것으로 처리합니다.
 * OBB(회전 상자)는 회전축 투영이 필요하므로 선택 심화 과제로 남깁니다.
 */
data class Aabb(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    init {
        require(left <= right) { "left must be <= right" }
        require(top <= bottom) { "top must be <= bottom" }
    }

    fun overlaps(other: Aabb): Boolean =
        right > other.left && left < other.right &&
            bottom > other.top && top < other.bottom

    companion object {
        fun fromCenter(x: Float, y: Float, width: Float, height: Float): Aabb {
            val halfWidth = width / 2f
            val halfHeight = height / 2f
            return Aabb(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight)
        }
    }
}
