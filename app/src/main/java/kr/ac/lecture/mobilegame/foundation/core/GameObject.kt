package kr.ac.lecture.mobilegame.foundation.core

import kr.ac.lecture.mobilegame.foundation.collision.Aabb
import kr.ac.lecture.mobilegame.foundation.collision.CollisionComponent
import kr.ac.lecture.mobilegame.foundation.component.MovementComponent
import kr.ac.lecture.mobilegame.foundation.component.SpriteComponent
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.Color

/** 위치·크기·속도처럼 X/Y 값 두 개가 필요한 곳에 사용합니다. 각 값은 변경할 수 있습니다. */
/** 게임 엔진의 Vector2 자료형에 대응됩니다. */
data class Vec2(var x: Float = 0f, var y: Float = 0f)

/** 물체의 위치, 회전 각도(degree), 크기 배율입니다. position은 GameObject와 같은 값을 공유합니다. */
class Transform(val position: Vec2, var rotation: Float = 0f, val scale: Vec2 = Vec2(1f, 1f))

/** 게임 물체의 공통 부분입니다. 필요한 그림·이동·충돌 기능만 붙여 사용합니다. */
abstract class GameObject(val position: Vec2, val size: Vec2) {
    // 각 기능이 서로 다른 위치를 사용하지 않도록 같은 Transform을 전달합니다.
    val transform = Transform(position)
    var sprite: SpriteComponent? = null
    var movement: MovementComponent? = null
    var collision: CollisionComponent? = null
    var active: Boolean = false     //잘못된 호출로 인해 오브젝트 스팸 발생시 대응. 명시적으로 활성화 필요.
    protected open val fallbackColor: Color = Color.WHITE
    protected val scaledSize get() = Vec2(size.x * transform.scale.x, size.y * transform.scale.y)

    val bounds: Aabb
        get() = collision?.bounds ?: Aabb.fromCenter(position.x, position.y, kotlin.math.abs(size.x * transform.scale.x), kotlin.math.abs(size.y * transform.scale.y))

    /** Override할 때 super.update()를 한 번 호출하면 부착된 기능도 갱신됩니다. */
    open fun update(deltaTime: Float) {
        movement?.update(deltaTime)
        sprite?.update(deltaTime)
    }

    /** 외형이 없을 때도 같은 Transform으로 사각형을 그려 이동·충돌 실습을 계속할 수 있습니다. */
    open fun draw(renderer: Renderer2D) {
        if (sprite?.currentRegion != null) {
            sprite?.draw(renderer)
        } else {
            renderer.drawRect(position, scaledSize, fallbackColor, transform.rotation)
        }
    }
}
