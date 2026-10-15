package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.BuildConfig
import kr.ac.lecture.mobilegame.foundation.collision.CollisionSystem
import kr.ac.lecture.mobilegame.foundation.debug.debugPrint
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot
import kr.ac.lecture.mobilegame.foundation.scene.Scene
import kr.ac.lecture.mobilegame.foundation.ui.DebugHud
import kr.ac.lecture.mobilegame.game.sprite.ShootingSprites
import kotlin.random.Random
import kr.ac.lecture.mobilegame.foundation.audio.SoundManager

/**
 * 작은 슈팅 예제입니다. 입력 → 생성 → 이동 → 충돌 → 제거 순서로 읽어 보세요.
 * 이동 속도는 GameConfig, 적 배치와 충돌 결과는 이 파일에서 바꿉니다.
 */
class ShooterScene(private val sprites: ShootingSprites? = null, private val sound: SoundManager? = null) : Scene {
    // TODO(학생 실습): 효과음을 한 번 load/register하고 원하는 게임 이벤트에서 play하세요.
    // 매 프레임 로드하지 않습니다. 채널 재등록 전 unregister가 필요합니다.
    // 그림을 다시 읽는 일과 소리를 재생하는 일은 따로 처리합니다.
    private val player = Player(sprites?.newPlayerIdleClip())
    private val bullets = mutableListOf<Bullet>()
    private val enemies = mutableListOf<Enemy>()
    private val explosions = mutableListOf<Explosion>()
    private val hud = DebugHud()
    private var playerShotElapsed = 0f
    private var enemySpawnElapsed = 0f
    private var playElapsed = 0f
    private var score = 0
    private var gameOver = false

    override fun update(deltaTime: Float, input: InputSnapshot) {
        if (!gameOver) {
            playElapsed += deltaTime
            player.readInput(input)
            player.update(deltaTime)
            if (BuildConfig.SAMPLE_BLOCK) updateShootingRules(deltaTime)
            for (bullet in bullets) if (bullet.active) bullet.update(deltaTime)
            for (enemy in enemies) if (enemy.active) enemy.update(deltaTime)
        }
        // 게임이 끝나도 이미 시작한 폭발은 마지막 그림까지 재생합니다.
        for (effect in explosions) if (effect.active) effect.update(deltaTime)
        if (!gameOver) resolveCollisions()

        // 1차 구현은 active 플래그 + removeAll. 후속 단계에서 ObjectPool로 교체합니다.
        bullets.removeAll { !it.active }
        enemies.removeAll { !it.active }
        explosions.removeAll { !it.active }
    }

    private fun updateShootingRules(deltaTime: Float) {
        playerShotElapsed += deltaTime
        while (playerShotElapsed >= GameConfig.FIRE_INTERVAL_SECONDS) {
            playerShotElapsed -= GameConfig.FIRE_INTERVAL_SECONDS
            bullets += Bullet(player.position.x, player.position.y + player.size.y, sprites?.playerBullet)
        }

        enemySpawnElapsed += deltaTime
        val spawnInterval = GameConfig.enemySpawnInterval(playElapsed)
        while (enemySpawnElapsed >= spawnInterval) {
            enemySpawnElapsed -= spawnInterval
            val enemyClip = if (Random.nextBoolean()) sprites?.newSmallEnemyClip() else sprites?.newBomberEnemyClip()
            enemies += Enemy(
                x = Random.nextFloat() * 1.7f - 0.85f,
                speed = GameConfig.enemySpeed(playElapsed),
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
                debugPrint("score=$score", display = false)
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
        for (bullet in bullets) if (bullet.active) bullet.draw(renderer)
        for (enemy in enemies) if (enemy.active) enemy.draw(renderer)
        for (effect in explosions) if (effect.active) effect.draw(renderer)
        hud.draw(renderer)
    }
}
