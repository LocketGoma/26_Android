package kr.ac.lecture.mobilegame.foundation.collision

import kr.ac.lecture.mobilegame.foundation.core.GameObject

object CollisionSystem {
    fun intersects(first: GameObject, second: GameObject): Boolean =
        first.active && second.active && first.bounds.overlaps(second.bounds)

    // TODO(선택 심화): 원 충돌과 OBB 충돌 전략을 별도 인터페이스로 분리합니다.
}
