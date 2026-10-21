package kr.ac.lecture.mobilegame.game.platformer

import kr.ac.lecture.mobilegame.foundation.physics.PlatformSurface
import kotlin.math.cos

enum class SurfaceLook { GROUND, WOOD }
enum class PickupKind(val points: Int) { APPLE(10), ACORN(20) }
enum class PlatformEnemyKind { CAT, SNAKE }

data class PlatformPickup(
    val id: Int, val x: Float, val y: Float, val kind: PickupKind, val inFinishPile: Boolean = false, var collected: Boolean = false
)

data class LevelCheckpoint(val x: Float, val surfaceId: Int, val triggerX: Float = x)

data class LevelSurface(
    val surface: PlatformSurface, val look: SurfaceLook, val fromX: Float = surface.x, val toX: Float = surface.x, val travelSeconds: Float = 12f
) {
    val moving get() = fromX != toX
    fun update(time: Float) {
        val amount = (1f - cos(time * (Math.PI * 2.0 / travelSeconds)).toFloat()) * 0.5f
        surface.moveTo(fromX + (toX - fromX) * amount, surface.y)
    }
}

/** 16칸짜리 화면 세 개를 이은 맵입니다. 배치만 바꾸어 다른 코스를 만들 수 있습니다. */
class PlatformerLevel {
    val checkpoints = listOf(LevelCheckpoint(1.4f, 1), LevelCheckpoint(16f, 3, 15.5f), LevelCheckpoint(31.5f, 30))
    val surfaces = listOf(ground(1, 0f, 5f), ground(2, 6.6f, 10.6f), ground(3, 12.1f, 18f), ledge(4, 3.1f, 4.7f, 2.35f), ledge(5, 7.5f, 9.3f, 2.7f), ledge(6, 12.7f, 14.3f, 2.45f),
        // 18..30의 큰 구덩이는 일반 점프 거리보다 훨씬 넓습니다.
        LevelSurface(PlatformSurface(20, 19.3f, 1.52f, 3.2f, 0.36f), SurfaceLook.WOOD, 19.3f, 28.7f, 12f), ground(30, 30f, 36.5f), ground(31, 39f, WIDTH), ledge(32, 32.5f, 34.2f, 2.4f), ledge(33, 35f, 36.5f, 3.1f), LevelSurface(PlatformSurface(34, 37.4f, 2.1f, 1.6f, 0.36f), SurfaceLook.WOOD, 37.4f, 38.2f, 4f))
    val collisionSurfaces = surfaces.map { it.surface }
    val pickups = mutableListOf<PlatformPickup>()
    val enemies = mutableListOf(PlatformEnemy(1, PlatformEnemyKind.CAT, 34.4f, FLOOR_TOP, 33f, 35.5f), PlatformEnemy(2, PlatformEnemyKind.SNAKE, 41.6f, FLOOR_TOP, 40.2f, 43.0f))

    init {
        listOf(2.3f to 2.0f, 4f to 3.1f, 7.4f to 2f, 8.3f to 3.5f, 13.2f to 3.2f, 16.3f to 2.1f, 31.5f to 2f, 33.3f to 3.1f, 35.6f to 3.9f, 37.8f to 3.2f, 40.1f to 2f).forEachIndexed { index, (x, y) ->
            pickups += PlatformPickup(index, x, y, if (index % 2 == 0) PickupKind.APPLE else PickupKind.ACORN)
        }
        // 위쪽 줄을 조금씩 좁히고 색과 위치를 섞어 아이템을 쌓아 둔 더미처럼 보이게 합니다.
        for (row in 0..5) {
            val count = 10 - row
            for (column in 0 until count) {
                val x = 45.6f + (column - (count - 1) / 2f) * 0.43f + ((column * 3 + row) % 5 - 2) * 0.025f
                val y = 1.45f + row * 0.39f + ((column + row * 2) % 3 - 1) * 0.04f
                pickups += PlatformPickup(100 + row * 10 + column, x, y, if ((row + column) % 3 == 0) PickupKind.ACORN else PickupKind.APPLE, true)
            }
        }
    }

    companion object {
        const val VIEW_WIDTH = 16f
        const val VIEW_HEIGHT = 9f
        const val WIDTH = VIEW_WIDTH * 3f
        const val FLOOR_TOP = 1.2f
        const val FINISH_X = 46.2f
        const val PLAYER_WIDTH = 0.72f
        const val PLAYER_HEIGHT = 0.92f
        const val PLAYER_MAX_LIFE = 3
        const val HURT_SECONDS = 0.4f
        const val RESPAWN_SECONDS = 0.85f
        private fun ground(id: Int, left: Float, right: Float) = LevelSurface(PlatformSurface(id, (left + right) / 2f, FLOOR_TOP / 2f, right - left, FLOOR_TOP), SurfaceLook.GROUND)
        private fun ledge(id: Int, left: Float, right: Float, top: Float) = LevelSurface(PlatformSurface(id, (left + right) / 2f, top - 0.18f, right - left, 0.36f), SurfaceLook.WOOD)
    }
}
