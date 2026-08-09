package kr.ac.lecture.mobilegame.foundation.core

import kr.ac.lecture.mobilegame.foundation.collision.Aabb
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/** Player, Enemy, Bullet이 공유하는 최소 Update/Draw 계약입니다. */
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
