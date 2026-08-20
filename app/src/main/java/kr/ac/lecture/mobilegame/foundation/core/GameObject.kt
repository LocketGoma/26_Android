package kr.ac.lecture.mobilegame.foundation.core

import kr.ac.lecture.mobilegame.foundation.collision.Aabb
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/**
 * GameObject의 위치와 크기, Renderer2D의 좌표 전달에 사용하는 mutable data class입니다.
 * 단독 동작이 없는 한 줄 선언이므로 가장 직접적인 사용처인 GameObject와 함께 둡니다.
 */
data class Vec2(var x: Float = 0f, var y: Float = 0f)

/** Player, Enemy, Bullet의 공통 Update/Draw 구조를 정의하는 Abstract Class입니다. */
abstract class GameObject(
    val position: Vec2,
    val size: Vec2,
) {
    var active: Boolean = true

    val bounds: Aabb
        get() = Aabb.fromCenter(position.x, position.y, size.x, size.y)

    abstract fun update(deltaTime: Float)
    abstract fun draw(renderer: Renderer2D)
}
