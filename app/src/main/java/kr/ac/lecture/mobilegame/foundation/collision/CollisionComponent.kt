package kr.ac.lecture.mobilegame.foundation.collision

import kr.ac.lecture.mobilegame.foundation.core.Transform
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kotlin.math.abs

/** [강사 제공 기반 시스템] 감지만 담당합니다. 피해·점수 처리는 game의 역할입니다. */
interface CollisionComponent {
    val bounds: Aabb
    fun intersects(other: CollisionComponent): Boolean
    // TODO(심화): OBB를 추가할 때 AABB↔OBB와 OBB↔OBB 판정도 함께 구현합니다.
    // bounds가 겹친다는 사실만으로 OBB의 실제 충돌을 확정하지 않습니다.
}

/**
 * Size/Offset은 NDC 월드 단위이며 Sprite 크기와 독립적입니다.
 * Transform Scale은 적용하지만 Rotation은 적용하지 않는 축 정렬 상자입니다.
 * 회전된 Sprite 전체를 감싸는 상자를 자동 계산하는 기능은 아닙니다.
 */
class AABBCollisionComponent(
    private val transform: Transform,
    val size: Vec2,
    val offset: Vec2 = Vec2(),
) : CollisionComponent {
    override val bounds: Aabb
        get() = Aabb.fromCenter(
            transform.position.x + offset.x * transform.scale.x,
            transform.position.y + offset.y * transform.scale.y,
            abs(size.x * transform.scale.x), abs(size.y * transform.scale.y),
        )

    override fun intersects(other: CollisionComponent): Boolean =
        if (other is AABBCollisionComponent) bounds.overlaps(other.bounds)
        else other.intersects(this) // 새 Shape가 기존 AABB와의 판정을 담당합니다.
}
