package kr.ac.lecture.mobilegame.foundation.component

import kr.ac.lecture.mobilegame.foundation.core.Transform
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kotlin.math.sqrt

/**
 * [강사 제공 기반 시스템] 목표 속도에 일정한 가속도로 접근합니다. 물리 엔진은 아닙니다.
 * 속도 단위는 NDC 월드 좌표/초, 가속도·감속도 단위는 NDC 월드 좌표/초²입니다.
 * 입력 방향이 일정한 구간의 이동을 적분하여 30/60 FPS 차이를 줄입니다.
 * 언제 어느 방향으로 움직일지는 game 패키지에서 결정합니다.
 */
class MovementComponent(
    private val transform: Transform,
    maxSpeed: Float = 1f,
    acceleration: Float = 4f,
    deceleration: Float = 4f,
) {
    var maxSpeed = maxSpeed
        set(value) { require(value.isFinite() && value >= 0f); field = value }
    var acceleration = acceleration
        set(value) { require(value.isFinite() && value >= 0f); field = value }
    var deceleration = deceleration
        set(value) { require(value.isFinite() && value >= 0f); field = value }
    // 초기 속도를 지정하면 기존 탄환처럼 처음부터 일정한 속도로 움직일 수 있습니다.
    val velocity = Vec2()
    private val direction = Vec2()

    init {
        require(listOf(maxSpeed, acceleration, deceleration).all { it.isFinite() && it >= 0f })
    }

    fun setMoveDirection(x: Float, y: Float) {
        require(x.isFinite() && y.isFinite())
        val length = kotlin.math.hypot(x.toDouble(), y.toDouble()).coerceAtLeast(1.0)
        direction.x = (x / length).toFloat()
        direction.y = (y / length).toFloat()
    }

    fun update(deltaTime: Float) {
        require(deltaTime.isFinite() && deltaTime >= 0f)
        val targetX = direction.x * maxSpeed
        val targetY = direction.y * maxSpeed
        val dx = targetX - velocity.x
        val dy = targetY - velocity.y
        val distance = sqrt(dx * dx + dy * dy)
        val rate = if (direction.x == 0f && direction.y == 0f) deceleration else acceleration
        val time = if (distance > 0f && rate > 0f) (distance / rate).coerceAtMost(deltaTime) else 0f
        val ratio = if (distance > 0f) (rate * time / distance).coerceAtMost(1f) else 0f
        val nextX = velocity.x + dx * ratio
        val nextY = velocity.y + dy * ratio
        // 가속 구간의 사다리꼴 적분 + 목표 속도에 도달한 뒤의 등속 구간입니다.
        transform.position.x += (velocity.x + nextX) * 0.5f * time + nextX * (deltaTime - time)
        transform.position.y += (velocity.y + nextY) * 0.5f * time + nextY * (deltaTime - time)
        velocity.x = nextX
        velocity.y = nextY
    }
}
