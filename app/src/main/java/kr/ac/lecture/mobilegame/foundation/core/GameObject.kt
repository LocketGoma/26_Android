package kr.ac.lecture.mobilegame.foundation.core

import kr.ac.lecture.mobilegame.foundation.collision.Aabb
import kr.ac.lecture.mobilegame.foundation.collision.CollisionComponent
import kr.ac.lecture.mobilegame.foundation.component.MovementComponent
import kr.ac.lecture.mobilegame.foundation.component.SpriteComponent
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/**
 * GameObject의 위치와 크기, Renderer2D의 좌표 전달에 사용하는 mutable data class입니다.
 * 단독 동작이 없는 한 줄 선언이므로 가장 직접적인 사용처인 GameObject와 함께 둡니다.
 */
data class Vec2(var x: Float = 0f, var y: Float = 0f)

/** Position은 기존 NDC 월드 좌표, Rotation은 degree, Scale은 배율입니다. */
class Transform(val position: Vec2, var rotation: Float = 0f, val scale: Vec2 = Vec2(1f, 1f))

/** Player, Enemy, Bullet의 공통 Update/Draw 구조를 정의하는 Abstract Class입니다. */
abstract class GameObject(
    val position: Vec2,
    val size: Vec2,
) {
    // 기존 position/size 접근을 유지합니다. Component는 모두 같은 Transform을 참조합니다.
    val transform = Transform(position)
    var sprite: SpriteComponent? = null
    var movement: MovementComponent? = null
    var collision: CollisionComponent? = null
    var active: Boolean = true

    val bounds: Aabb
        get() = collision?.bounds ?: Aabb.fromCenter(position.x, position.y,
            kotlin.math.abs(size.x * transform.scale.x), kotlin.math.abs(size.y * transform.scale.y))

    /** Override할 때 super.update()를 한 번 호출하면 부착된 기능도 갱신됩니다. */
    open fun update(deltaTime: Float) {
        movement?.update(deltaTime)
        sprite?.update(deltaTime)
    }
    open fun draw(renderer: Renderer2D) { sprite?.draw(renderer) }
}
