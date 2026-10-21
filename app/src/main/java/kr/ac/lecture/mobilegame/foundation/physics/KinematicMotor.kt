package kr.ac.lecture.mobilegame.foundation.physics

import kr.ac.lecture.mobilegame.foundation.collision.Aabb
import kotlin.math.abs

/** 게임 안에서 정한 길이 단위를 사용합니다. +Y는 위쪽이며 그림이나 Android 화면과 따로 계산합니다. */
class PlatformBody(var x: Float, var y: Float, val width: Float = 0.7f, val height: Float = 1f) {
    var velocityX = 0f
    var velocityY = 0f
    var supportId: Int? = null
    val bounds get() = Aabb.fromCenter(x, y, width, height)
    val feet get() = y - height / 2f
}

/** update에서 위치를 먼저 바꾸고 그 프레임의 실제 이동량을 기록합니다. */
class PlatformSurface(val id: Int, var x: Float, var y: Float, val width: Float, val height: Float) {
    var deltaX = 0f
        private set
    var deltaY = 0f
        private set
    val left get() = x - width / 2f
    val right get() = x + width / 2f
    val top get() = y + height / 2f
    val bottom get() = y - height / 2f

    fun moveTo(nextX: Float, nextY: Float) {
        deltaX = nextX - x
        deltaY = nextY - y
        x = nextX
        y = nextY
    }
}

data class MotorSettings(
    val acceleration: Float = 24f,
    val deceleration: Float = 30f,
    val maxSpeed: Float = 5f,
    val gravity: Float = 22f,
    val jumpSpeed: Float = 9f,
) {
    init {
        require(listOf(acceleration, deceleration, maxSpeed, gravity, jumpSpeed).all { it.isFinite() && it >= 0f })
    }
}

/** 가로·세로 이동과 충돌을 따로 계산합니다. 빠른 물체의 통과나 발판 사이에 끼이는 상황은 추가 처리가 필요합니다. */
class KinematicMotor(val settings: MotorSettings = MotorSettings()) {
    fun step(
        body: PlatformBody, surfaces: List<PlatformSurface>, axis: Float, jump: Boolean, dt: Float, followPlatform: Boolean = true
    ) {
        require(dt.isFinite() && dt >= 0f && axis.isFinite())
        applyPlatformMovement(body, surfaces, jump, followPlatform)
        moveHorizontally(body, surfaces, axis, dt)
        moveVertically(body, surfaces, dt)
    }

    private fun applyPlatformMovement(
        body: PlatformBody, surfaces: List<PlatformSurface>, jump: Boolean, followPlatform: Boolean
    ) {
        val support = surfaces.firstOrNull { it.id == body.supportId }
        val wasSupported = support != null && abs(body.feet - (support.top - support.deltaY)) < 0.04f && body.x + body.width / 2f > support.left - support.deltaX && body.x - body.width / 2f < support.right - support.deltaX

        // 점프하면 발판을 떠나므로 이번 계산부터 발판의 이동량을 캐릭터에 더하지 않습니다.
        if (jump && wasSupported) body.velocityY = settings.jumpSpeed
        else if (wasSupported && followPlatform) {
            body.x += support!!.deltaX
            body.y += support.deltaY
        }
        body.supportId = null
    }

    private fun moveHorizontally(body: PlatformBody, surfaces: List<PlatformSurface>, axis: Float, dt: Float) {
        val target = axis.coerceIn(-1f, 1f) * settings.maxSpeed
        val rate = if (axis == 0f) settings.deceleration else settings.acceleration
        val oldVelocity = body.velocityX
        body.velocityX = moveTowards(oldVelocity, target, rate * dt)
        val oldX = body.x
        body.x += (oldVelocity + body.velocityX) * 0.5f * dt
        for (surface in surfaces) {
            if (body.y + body.height / 2f <= surface.bottom + EPSILON || body.feet >= surface.top - EPSILON) continue
            if (body.velocityX > 0f && oldX + body.width / 2f <= surface.left + EPSILON && body.x + body.width / 2f > surface.left) {
                body.x = surface.left - body.width / 2f; body.velocityX = 0f
            } else if (body.velocityX < 0f && oldX - body.width / 2f >= surface.right - EPSILON && body.x - body.width / 2f < surface.right) {
                body.x = surface.right + body.width / 2f; body.velocityX = 0f
            }
        }
    }

    private fun moveVertically(body: PlatformBody, surfaces: List<PlatformSurface>, dt: Float) {
        val previousFeet = body.feet
        val previousHead = body.y + body.height / 2f
        body.velocityY -= settings.gravity * dt
        body.y += body.velocityY * dt
        val falling = body.velocityY <= 0f
        var hit: PlatformSurface? = null
        // 매 계산마다 지형 전체를 정렬하지 않고 가장 먼저 닿는 윗면/아랫면을 고릅니다.
        for (surface in surfaces) {
            if (body.x + body.width / 2f <= surface.left || body.x - body.width / 2f >= surface.right) continue
            if (falling && previousFeet >= surface.top - maxOf(0f, surface.deltaY) - EPSILON && body.feet <= surface.top) {
                if (hit == null || surface.top > hit.top) hit = surface
            } else if (!falling && previousHead <= surface.bottom + EPSILON && body.y + body.height / 2f > surface.bottom) {
                if (hit == null || surface.bottom < hit.bottom) hit = surface
            }
        }
        hit?.let { surface ->
            body.y = if (falling) surface.top + body.height / 2f else surface.bottom - body.height / 2f
            body.velocityY = 0f
            if (falling) body.supportId = surface.id
        }
    }

    private fun moveTowards(value: Float, target: Float, amount: Float) = when {
        value < target -> minOf(value + amount, target)
        else -> maxOf(value - amount, target)
    }

    companion object {
        private const val EPSILON = 0.002f
    }
}
