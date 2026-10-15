package kr.ac.lecture.mobilegame.game.platformer

import kr.ac.lecture.mobilegame.foundation.collision.Aabb
import kr.ac.lecture.mobilegame.foundation.input.DigitalSnapshot
import kr.ac.lecture.mobilegame.foundation.physics.KinematicMotor
import kr.ac.lecture.mobilegame.foundation.physics.PlatformBody

enum class PlatformerState { PLAYING, DEAD, CLEARED }

data class PlatformerUiSnapshot(val state: PlatformerState, val section: Int, val life: Int,
                               val collected: Int, val score: Int, val speed: Float, val maxSpeed: Float,
                               val followPlatform: Boolean, val showCollision: Boolean)

/** 위치와 게임 규칙만 계산합니다. 그림, Android View, OpenGL은 이 클래스에서 사용하지 않습니다. */
class PlatformerWorld {
    var level = PlatformerLevel()
        private set
    val player = PlatformBody(1.4f, PlatformerLevel.FLOOR_TOP + 0.46f, 0.72f, 0.92f)
    val motor = KinematicMotor()
    var state = PlatformerState.PLAYING
        private set
    var life = 3
        private set
    var score = 0
        private set
    var collected = 0
        private set
    var facing = 1
        private set
    var elapsed = 0f
        private set
    var deadElapsed = 0f
        private set
    var hurtRemaining = 0f
        private set
    var jumpAge = 100f
        private set
    var followPlatform = true
    var showCollision = false
    var viewHeight = PlatformerLevel.VIEW_HEIGHT
        private set
    private var invincibleRemaining = 0f
    private var platformTime = 0f
    private var checkpointSection = 1
    private var pendingJump = false
    val section get() = (player.x / PlatformerLevel.VIEW_WIDTH).toInt().coerceIn(0, 2) + 1
    val cameraLeft get() = (player.x - 6f).coerceIn(0f, PlatformerLevel.WIDTH - PlatformerLevel.VIEW_WIDTH)
    val blinkPlayer get() = invincibleRemaining > 0f && (elapsed / 0.09f).toInt() % 2 == 0

    fun resizeViewport(width: Int, height: Int) {
        if (width > 0 && height > 0) viewHeight = PlatformerLevel.VIEW_WIDTH * height / width
    }

    fun restart() {
        level = PlatformerLevel()
        score = 0
        collected = 0
        elapsed = 0f
        checkpointSection = 1
        respawn()
    }

    fun update(dt: Float, input: DigitalSnapshot) {
        val frameTime = dt.coerceIn(0f, 0.05f)
        elapsed += frameTime
        if (state == PlatformerState.DEAD) {
            pendingJump = false
            deadElapsed += frameTime
            if (deadElapsed >= 0.85f) respawn()
            return
        }
        if (state == PlatformerState.CLEARED) return
        pendingJump = pendingJump || input.jumpPressed
        var remaining = frameTime
        // 한 프레임을 짧게 나누어 발판과 캐릭터를 같은 시간 간격으로 움직입니다.
        while (remaining > 0f && state == PlatformerState.PLAYING) {
            val stepTime = minOf(remaining, MAX_STEP)
            step(stepTime, input.axis, pendingJump)
            pendingJump = false
            remaining = (remaining - stepTime).coerceAtLeast(0f)
        }
    }

    private fun step(dt: Float, axis: Float, jump: Boolean) {
        invincibleRemaining = (invincibleRemaining - dt).coerceAtLeast(0f)
        hurtRemaining = (hurtRemaining - dt).coerceAtLeast(0f)
        jumpAge += dt
        if (axis != 0f) facing = if (axis > 0f) 1 else -1

        // 발판의 이번 이동량을 먼저 구합니다. 그다음 캐릭터가 그 이동량을 따라갑니다.
        platformTime += dt
        level.surfaces.forEach { it.update(platformTime) }
        val previousFeet = player.feet
        if (jump && player.supportId != null) jumpAge = 0f
        motor.step(player, level.collisionSurfaces, axis, jump, dt, followPlatform)
        player.x = player.x.coerceIn(player.width / 2f, PlatformerLevel.WIDTH - player.width / 2f)
        level.enemies.forEach { it.update(dt) }

        resolveEnemies(previousFeet)
        if (state != PlatformerState.PLAYING) return
        if (player.feet < -1f) { die(fell = true); return }
        collectItems()
        if (player.supportId != null) {
            if (player.x >= 31.5f) checkpointSection = 3
            else if (player.x >= 15.5f) checkpointSection = maxOf(checkpointSection, 2)
        }
        if (player.x >= PlatformerLevel.FINISH_X && player.supportId != null && player.feet < 2.5f) {
            state = PlatformerState.CLEARED
            player.velocityX = 0f
            player.velocityY = 0f
        }
    }

    private fun resolveEnemies(previousFeet: Float) {
        for (enemy in level.enemies) {
            if (!enemy.alive) continue
            val head = enemy.headBounds
            val overlapsHeadX = player.x + player.width / 2f > head.left && player.x - player.width / 2f < head.right
            if (player.velocityY <= 0f && previousFeet >= head.bottom - 0.025f &&
                player.feet <= head.bottom && overlapsHeadX) {
                enemy.stomp()
                awardEnemy(enemy)
                player.y = head.bottom + player.height / 2f + 0.002f
                player.velocityY = 6.5f
                player.supportId = null
                continue // 같은 계산에서 머리 밟기와 몸통 피격이 함께 일어나지 않게 합니다.
            }
            if (player.bounds.overlaps(enemy.bodyBounds) || player.bounds.overlaps(head)) takeHit(enemy.x)
            if (state != PlatformerState.PLAYING) return
        }
    }

    private fun takeHit(enemyX: Float) {
        if (invincibleRemaining > 0f) return
        life -= 1
        hurtRemaining = 0.4f
        invincibleRemaining = 1.1f
        player.velocityX = if (player.x < enemyX) -4f else 4f
        player.velocityY = 4.5f
        player.supportId = null
        if (life <= 0) die(fell = false)
    }

    private fun awardEnemy(enemy: PlatformEnemy) { score += if (enemy.kind == PlatformEnemyKind.CAT) 100 else 150 }

    private fun collectItems() {
        for (pickup in level.pickups) {
            if (!pickup.collected && player.bounds.overlaps(Aabb.fromCenter(pickup.x, pickup.y, 0.4f, 0.4f))) {
                pickup.collected = true
                collected += 1
                score += pickup.kind.points
            }
        }
    }

    private fun die(fell: Boolean) {
        state = PlatformerState.DEAD
        life = 0
        deadElapsed = 0f
        player.velocityX = 0f
        player.velocityY = 0f
        if (fell) player.y = player.height / 2f + 0.08f
    }

    private fun respawn() {
        state = PlatformerState.PLAYING
        life = 3
        deadElapsed = 0f
        hurtRemaining = 0f
        invincibleRemaining = 0f
        pendingJump = false
        platformTime = 0f
        level.surfaces.forEach { it.update(0f) }
        player.x = when (checkpointSection) { 2 -> 16f; 3 -> 31.5f; else -> 1.4f }
        player.y = PlatformerLevel.FLOOR_TOP + player.height / 2f
        player.velocityX = 0f; player.velocityY = 0f
        player.supportId = when (checkpointSection) { 2 -> 3; 3 -> 30; else -> 1 }
        facing = 1
        jumpAge = 100f
    }

    fun uiSnapshot() = PlatformerUiSnapshot(state, section, life, collected, score, player.velocityX,
        motor.settings.maxSpeed, followPlatform, showCollision)

    companion object { const val MAX_STEP = 1f / 120f }
}
