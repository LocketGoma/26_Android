package kr.ac.lecture.mobilegame.game.platformer

import kr.ac.lecture.mobilegame.foundation.collision.Aabb

/** 적은 평평한 바닥 위에서 왕복합니다. 그림은 이동 애니메이션 하나만 사용합니다. */
class PlatformEnemy(val id: Int, val kind: PlatformEnemyKind, var x: Float, val feet: Float,
                    private val leftLimit: Float, private val rightLimit: Float) {
    var facing = 1
        private set
    var alive = true
        private set
    val width get() = if (kind == PlatformEnemyKind.CAT) 0.85f else 1.85f
    val height get() = if (kind == PlatformEnemyKind.CAT) 0.82f else 0.74f
    val bounds get() = Aabb.fromCenter(x, feet + height / 2f, width, height)

    // 뱀은 오른쪽(왼쪽 보기에서는 왼쪽)에 들린 머리만 밟을 수 있습니다.
    val headBounds: Aabb get() {
        val offset = if (kind == PlatformEnemyKind.CAT) 0.22f else 0.62f
        val headHeight = if (kind == PlatformEnemyKind.CAT) 0.24f else 0.36f
        return Aabb.fromCenter(x + facing * offset, feet + height - headHeight / 2f, 0.5f, headHeight)
    }
    val bodyBounds get() = if (kind == PlatformEnemyKind.CAT) bounds
        else Aabb.fromCenter(x, feet + 0.2f, width, 0.4f)

    fun update(dt: Float) {
        if (!alive) return
        x += facing * (if (kind == PlatformEnemyKind.CAT) 1.05f else 0.8f) * dt
        if (x >= rightLimit) { x = rightLimit; facing = -1 }
        if (x <= leftLimit) { x = leftLimit; facing = 1 }
    }

    fun stomp() { alive = false }
}
