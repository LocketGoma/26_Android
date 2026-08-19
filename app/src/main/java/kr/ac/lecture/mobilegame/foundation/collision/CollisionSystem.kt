package kr.ac.lecture.mobilegame.foundation.collision

import kr.ac.lecture.mobilegame.foundation.core.GameObject

object CollisionSystem {
    fun intersects(first: GameObject, second: GameObject): Boolean =
        first.active && second.active && first.bounds.overlaps(second.bounds)

    // TODO(선택 심화): Circle Collision과 OBB Collision을 별도 Strategy Interface로 분리합니다.
}
