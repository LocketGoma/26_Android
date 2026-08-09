package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.BuildConfig
import kr.ac.lecture.mobilegame.foundation.collision.CollisionSystem
import kr.ac.lecture.mobilegame.foundation.debug.debugPrint
import kr.ac.lecture.mobilegame.foundation.debug.debugString
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot
import kr.ac.lecture.mobilegame.foundation.scene.Scene
import kr.ac.lecture.mobilegame.foundation.ui.DebugHud
import kr.ac.lecture.mobilegame.game.sprite.ShootingSprites
import kotlin.random.Random

/**
 * 학생 수정 중심 영역: 드래곤플라이트/1945식 세로 자동 슈팅의 규칙 초안입니다.
 * AI가 생성·수정한 코드도 각 update/draw/충돌 규칙의 역할을 학생 본인이 설명할 수 있어야 합니다.
 */
class ShooterScene(private val sprites: ShootingSprites? = null) : Scene {
    private val player = Player(sprites?.newPlayerIdleClip())
    private val bullets = mutableListOf<Bullet>()
    private val enemies = mutableListOf<Enemy>()
    private val explosions = mutableListOf<Explosion>()
    private val hud = DebugHud()
    private var shotTimer = 0f
    private var spawnTimer = 0f
    private var elapsed = 0f
    private var score = 0
    private var gameOver = false

    override fun update(deltaTime: Float, input: InputSnapshot) {
        if (gameOver) return
        elapsed += deltaTime
        player.readInput(input)
        player.update(deltaTime)

        // SAMPLE BLOCK: false로 빌드하면 아래 제공 샘플 규칙을 실행하지 않습니다.
        if (BuildConfig.SAMPLE_BLOCK) {
            updateSampleRules(deltaTime)
        }

        bullets.forEach { it.update(deltaTime) }
        enemies.forEach { it.update(deltaTime) }
        explosions.forEach { it.update(deltaTime) }
        resolveCollisions()

        // 1차 구현은 active 플래그 + removeAll. 후속 단계에서 ObjectPool로 교체합니다.
        bullets.removeAll { !it.active }
        enemies.removeAll { !it.active }
        explosions.removeAll { !it.active }
    }

    private fun updateSampleRules(deltaTime: Float) {
        shotTimer += deltaTime
        if (shotTimer >= GameConfig.FIRE_INTERVAL_SECONDS) {
            shotTimer -= GameConfig.FIRE_INTERVAL_SECONDS
            bullets += Bullet(player.position.x, player.position.y + player.size.y, sprites?.playerBullet)
        }

        spawnTimer += deltaTime
        val spawnInterval = (1.0f - elapsed * 0.01f).coerceAtLeast(0.35f) // Difficulty 초안
        if (spawnTimer >= spawnInterval) {
            spawnTimer -= spawnInterval
            val enemyClip = if (Random.nextBoolean()) sprites?.newSmallEnemyClip() else sprites?.newBomberEnemyClip()
            enemies += Enemy(
                x = Random.nextFloat() * 1.7f - 0.85f,
                speed = 0.42f + elapsed * 0.004f,
                sprite = enemyClip,
            )
        }
    }

    private fun resolveCollisions() {
        bullets.forEach { bullet ->
            enemies.firstOrNull { CollisionSystem.intersects(bullet, it) }?.let { enemy ->
                bullet.active = false
                enemy.active = false
                sprites?.explosion?.let { explosions += Explosion(enemy.position.x, enemy.position.y, it) }
                score += GameConfig.ENEMY_SCORE
                hud.updateScore(score)
                debugPrint("score=${debugString(score)}", display = false)
            }
        }
        if (enemies.any { CollisionSystem.intersects(player, it) }) {
            gameOver = true
            hud.showGameOver(true)
            debugPrint("Game Over")
        }
    }

    override fun draw(renderer: Renderer2D) {
        player.draw(renderer)
        bullets.filter { it.active }.forEach { it.draw(renderer) }
        enemies.filter { it.active }.forEach { it.draw(renderer) }
        explosions.filter { it.active }.forEach { it.draw(renderer) }
        hud.draw(renderer)
    }
}
