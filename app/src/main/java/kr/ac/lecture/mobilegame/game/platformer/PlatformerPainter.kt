package kr.ac.lecture.mobilegame.game.platformer

import kr.ac.lecture.mobilegame.foundation.collision.Aabb
import kr.ac.lecture.mobilegame.BuildConfig
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import kotlin.math.abs
import kotlin.math.sin

/** 가로 16칸을 화면 전체에 그립니다. 세로 범위를 기기 비율에 맞춰 그림과 충돌의 비율을 유지합니다. */
class PlatformerPainter(private val art: PlatformerSprites) {
    private var viewHeight = PlatformerLevel.VIEW_HEIGHT
    fun draw(renderer: Renderer2D, world: PlatformerWorld) {
        viewHeight = world.viewHeight
        val showCollision = BuildConfig.DEBUG_BLOCK && world.showCollision
        renderer.drawSprite(art.background, Vec2(), Vec2(2f, 2f))
        val camera = world.cameraLeft
        fun rect(x: Float, y: Float, width: Float, height: Float, color: Color) {
            renderer.drawRect(point(x, y, camera), size(width, height), color)
        }

        fun picture(
            region: SpriteRegion, x: Float, y: Float, width: Float, height: Float, flip: Boolean = false, rotation: Float = 0f
        ) {
            renderer.drawSprite(region, point(x, y, camera), size(width, height), rotation = rotation, flipX = flip)
        }

        fun outline(bounds: Aabb, color: Color) {
            val width = bounds.right - bounds.left
            val height = bounds.maxY - bounds.minY
            val x = (bounds.left + bounds.right) / 2f
            val y = (bounds.minY + bounds.maxY) / 2f
            rect(x, bounds.minY, width, 0.025f, color)
            rect(x, bounds.maxY, width, 0.025f, color)
            rect(bounds.left, y, 0.025f, height, color)
            rect(bounds.right, y, 0.025f, height, color)
        }
        for (entry in world.level.surfaces) {
            val surface = entry.surface
            if (surface.right < camera || surface.left > camera + PlatformerLevel.VIEW_WIDTH) continue
            picture(if (entry.look == SurfaceLook.GROUND) art.ground else art.wood, surface.x, surface.y, surface.width, surface.height)
            if (showCollision) outline(Aabb.fromCenter(surface.x, surface.y, surface.width, surface.height), Color.WHITE)
        }
        // 다음 구역의 다시 시작할 위치를 작은 깃발로 표시합니다.
        for (index in 1 until world.level.checkpoints.size) {
            val x = world.level.checkpoints[index].x
            rect(x, 1.7f, 0.055f, 1f, Color(0.25f, 0.36f, 0.25f))
            rect(x + 0.2f, 2.05f, 0.4f, 0.25f, Color.CYAN)
        }
        // 피니시 깃발은 아이템 더미 뒤에 세웁니다.
        rect(47.4f, 2.9f, 0.07f, 3.4f, Color(0.35f, 0.28f, 0.16f))
        rect(47.0f, 4.35f, 0.85f, 0.55f, Color.YELLOW)
        for (pickup in world.level.pickups) {
            if (pickup.collected || pickup.x < camera - 1f || pickup.x > camera + PlatformerLevel.VIEW_WIDTH + 1f) continue
            val region = if (pickup.kind == PickupKind.APPLE) art.apple else art.acorn
            val bob = if (pickup.inFinishPile) 0f else sin(world.elapsed * 3f + pickup.id) * 0.06f
            val tilt = if (pickup.inFinishPile) ((pickup.id % 5) - 2) * 9f else 0f
            picture(region, pickup.x, pickup.y + bob, 0.4f, 0.47f, rotation = tilt)
        }
        for (enemy in world.level.enemies) {
            if (!enemy.alive || enemy.x < camera - 2f || enemy.x > camera + PlatformerLevel.VIEW_WIDTH + 2f) continue
            val frames = if (enemy.kind == PlatformEnemyKind.CAT) art.cat else art.snake
            val frame = frames[(world.elapsed * (if (enemy.kind == PlatformEnemyKind.CAT) 8f else 7f)).toInt() % frames.size]
            val height = 1.3f
            val width = if (enemy.kind == PlatformEnemyKind.CAT) height else height * 2f
            picture(frame.region, enemy.x, enemy.feet + height * (frame.feet - 0.5f), width, height, enemy.facing < 0)
            if (showCollision) {
                outline(enemy.bodyBounds, Color.RED)
                outline(enemy.headBounds, Color.CYAN)
            }
        }
        if (!world.blinkPlayer || world.state != PlatformerState.PLAYING) {
            val frame = playerFrame(world)
            val height = 1.3f
            picture(frame.region, world.player.x, world.player.feet + height * (frame.feet - 0.5f), height, height, world.facing < 0)
        }
        if (showCollision) outline(world.player.bounds, Color(0.05f, 0.65f, 0.15f))
    }

    private fun playerFrame(world: PlatformerWorld): FrameArt = when {
        world.state == PlatformerState.DEAD -> art.death[(world.deadElapsed * 6f).toInt().coerceIn(0, art.death.lastIndex)]
        world.hurtRemaining > 0f -> art.hurt[((PlatformerLevel.HURT_SECONDS - world.hurtRemaining) * 10f).toInt().coerceIn(0, art.hurt.lastIndex)]
        world.player.supportId == null -> art.jump[when {
            world.jumpAge < 0.05f -> 0
            abs(world.player.velocityY) < 1.1f -> 2
            world.player.velocityY > 0f -> 1
            else -> 3
        }]

        abs(world.player.velocityX) > 0.1f -> art.run[(world.elapsed * 10f).toInt() % art.run.size]
        else -> art.idle[(world.elapsed * 5f).toInt() % art.idle.size]
    }

    private fun point(x: Float, y: Float, camera: Float) = Vec2((x - camera) / PlatformerLevel.VIEW_WIDTH * 2f - 1f, y / viewHeight * 2f - 1f)
    private fun size(width: Float, height: Float) = Vec2(width / PlatformerLevel.VIEW_WIDTH * 2f, height / viewHeight * 2f)
}
