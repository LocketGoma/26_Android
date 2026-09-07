package kr.ac.lecture.mobilegame.foundation.collision

import kr.ac.lecture.mobilegame.foundation.core.GameObject

object CollisionSystem {
    fun intersects(first: GameObject, second: GameObject): Boolean {
        if (!first.active || !second.active) return false
        val firstShape = first.collision ?: return false
        val secondShape = second.collision ?: return false
        return firstShape.intersects(secondShape)
    }

    // CollisionComponent가 없는 배경/효과는 충돌 대상이 아닙니다.
}
