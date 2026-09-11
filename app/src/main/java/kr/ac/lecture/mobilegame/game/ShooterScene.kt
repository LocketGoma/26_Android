package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.BuildConfig
import kr.ac.lecture.mobilegame.foundation.audio.SoundManager
import kr.ac.lecture.mobilegame.foundation.audio.ToneEffect
import kr.ac.lecture.mobilegame.foundation.collision.CollisionSystem
import kr.ac.lecture.mobilegame.foundation.debug.debugPrint
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot
import kr.ac.lecture.mobilegame.foundation.scene.Scene
import kr.ac.lecture.mobilegame.game.sprite.ItemSprites
import kr.ac.lecture.mobilegame.game.sprite.ShootingSprites
import kr.ac.lecture.mobilegame.game.sprite.UiSprites
import kotlin.random.Random

/** Cycle 기반 무한 반복형 세로 슈팅의 게임 규칙을 연결하는 장면입니다. */
class ShooterScene(
    private val sprites: ShootingSprites? = null,
    private val itemSprites: ItemSprites? = null,
    uiSprites: UiSprites? = null,
    private val leaderboard: LeaderboardStore? = null,
    private val uiListener: GameUiListener? = null,
    private val sound: SoundManager? = null,
) : Scene {
    // 별도 음원 Asset이 없어도 ToneEffect로 피드백하며, 추후 SoundPool Asset으로 교체할 수 있습니다.
    private val session = GameSession()
    private val player = Player(sprites?.newPlayerIdleClip())
    private val bullets = mutableListOf<Bullet>()
    private val enemies = mutableListOf<Enemy>()
    private val items = mutableListOf<ItemPickup>()
    private val explosions = mutableListOf<Explosion>()
    private val screenFlashes = mutableListOf<ScreenFlash>()
    private val hud = GameHud(uiSprites)
    private var shotTimer = 0f
    private var spawnTimer = 0f
    private var enemyShotTimer = 0f
    private var gameOverSaved = false

    override fun onEnter() = notifyUi()

    fun startGame() {
        clearWorld()
        player.reset()
        session.startGame()
        gameOverSaved = false
        notifyUi()
    }

    fun openLeaderboard() {
        session.openLeaderboard()
        notifyUi()
    }

    fun closeLeaderboard() {
        session.closeLeaderboard()
        notifyUi()
    }

    fun useBomb() {
        if (!session.useBomb()) return

        sound?.playTone(ToneEffect.BOMB)
        screenFlashes += ScreenFlash(Color(1f, 0.72f, 0.15f, 0.46f), 0.34f)
        sprites?.explosion?.let {
            explosions += Explosion(player.position.x, player.position.y, it, effectSize = 1.25f)
        }
        bullets.filter { it.owner == BulletOwner.ENEMY }.forEach { it.active = false }
        enemies.filter { it.active }.toList().forEach { enemy ->
            sprites?.impact?.let {
                explosions += Explosion(enemy.position.x, enemy.position.y, it,
                    effectSize = if (enemy.isBoss) 0.58f else 0.34f)
            }
            if (enemy.damage(GameConfig.BOMB_DAMAGE)) onEnemyKilled(enemy)
        }
        notifyUi()
    }

    override fun update(deltaTime: Float, input: InputSnapshot) {
        if (!session.isPlaying) {
            if (session.state == GameState.GAME_OVER) {
                explosions.forEach { it.update(deltaTime) }
                screenFlashes.forEach { it.update(deltaTime) }
                explosions.removeAll { !it.active }
                screenFlashes.removeAll { !it.active }
            }
            return
        }

        val bossPhaseStarted = session.update(deltaTime)
        if (bossPhaseStarted) spawnBoss()

        player.readInput(input)
        player.update(deltaTime)

        if (BuildConfig.SAMPLE_BLOCK) updateShootingRules(deltaTime)

        bullets.forEach { it.update(deltaTime) }
        enemies.forEach { it.update(deltaTime) }
        items.forEach { it.update(deltaTime) }
        explosions.forEach { it.update(deltaTime) }
        screenFlashes.forEach { it.update(deltaTime) }
        resolveCollisions()

        bullets.removeAll { !it.active }
        enemies.removeAll { !it.active }
        items.removeAll { !it.active }
        explosions.removeAll { !it.active }
        screenFlashes.removeAll { !it.active }
    }

    private fun updateShootingRules(deltaTime: Float) {
        shotTimer += deltaTime
        while (shotTimer >= GameConfig.FIRE_INTERVAL_SECONDS) {
            shotTimer -= GameConfig.FIRE_INTERVAL_SECONDS
            bullets += Bullet(player.position.x, player.position.y + player.size.y,
                sprites?.playerBullet, BulletOwner.PLAYER)
        }

        spawnTimer += deltaTime
        val spawnInterval = if (session.state == GameState.NORMAL) {
            GameConfig.normalSpawnInterval(session.cycle)
        } else {
            GameConfig.bossSpawnInterval(session.cycle)
        }
        while (spawnTimer >= spawnInterval) {
            spawnTimer -= spawnInterval
            spawnNormalEnemy()
        }

        enemyShotTimer += deltaTime
        val enemyFireInterval = if (session.state == GameState.BOSS) 0.75f
        else GameConfig.ENEMY_FIRE_INTERVAL_SECONDS
        if (enemyShotTimer >= enemyFireInterval) {
            enemyShotTimer -= enemyFireInterval
            enemies.filter { it.active }.randomOrNull()?.let { enemy ->
                bullets += Bullet(enemy.position.x, enemy.position.y - enemy.size.y / 2f,
                    sprites?.enemyBullet, BulletOwner.ENEMY)
            }
        }
    }

    private fun spawnNormalEnemy() {
        val kind = if (Random.nextFloat() < GameConfig.ELITE_SPAWN_CHANCE) {
            EnemyKind.ELITE
        } else {
            EnemyKind.NORMAL
        }
        val clip = if (kind == EnemyKind.ELITE) sprites?.newBomberEnemyClip() else sprites?.newSmallEnemyClip()
        enemies += Enemy(
            x = Random.nextFloat() * 1.7f - 0.85f,
            speed = (if (kind == EnemyKind.ELITE) 0.30f else 0.46f) +
                (session.cycle - 1) * 0.025f,
            sprite = clip,
            kind = kind,
            maxHp = if (kind == EnemyKind.ELITE) GameConfig.ELITE_ENEMY_HP else GameConfig.NORMAL_ENEMY_HP,
        )
    }

    private fun spawnBoss() {
        enemies.filter { it.isBoss }.forEach { it.active = false }
        enemies += Enemy(
            x = 0f,
            speed = 0f,
            sprite = sprites?.newBomberEnemyClip(),
            kind = EnemyKind.BOSS,
            maxHp = GameConfig.bossMaxHp(session.cycle),
        )
        spawnTimer = 0f
        enemyShotTimer = 0f
        notifyUi()
    }

    private fun resolveCollisions() {
        bullets.filter { it.owner == BulletOwner.PLAYER && it.active }.forEach { bullet ->
            enemies.firstOrNull { CollisionSystem.intersects(bullet, it) }?.let { enemy ->
                bullet.active = false
                sprites?.impact?.let {
                    explosions += Explosion(enemy.position.x, enemy.position.y, it,
                        effectSize = if (enemy.isBoss) 0.32f else 0.22f)
                }
                if (enemy.damage(GameConfig.PLAYER_BULLET_DAMAGE)) onEnemyKilled(enemy)
            }
        }

        bullets.filter { it.owner == BulletOwner.ENEMY && it.active }.forEach { bullet ->
            if (CollisionSystem.intersects(player, bullet)) {
                bullet.active = false
                handlePlayerHit()
            }
        }

        enemies.firstOrNull { CollisionSystem.intersects(player, it) }?.let { enemy ->
            if (!enemy.isBoss && !session.isInvincible) enemy.active = false
            handlePlayerHit()
        }

        items.filter { it.active }.forEach { item ->
            if (!CollisionSystem.intersects(player, item)) return@forEach
            item.active = false
            when (item.type) {
                ItemType.LIFE -> session.collectLife()
                ItemType.BOMB -> session.collectBomb()
            }
            notifyUi()
        }
    }

    private fun handlePlayerHit() {
        if (!session.takeHit()) return
        sound?.playTone(ToneEffect.HIT)
        screenFlashes += ScreenFlash(Color(1f, 0.08f, 0.04f, 0.52f), 0.28f)
        sprites?.impact?.let {
            explosions += Explosion(player.position.x, player.position.y, it, effectSize = 0.68f)
        }
        sprites?.playerHitFrames?.let {
            explosions += Explosion(player.position.x, player.position.y, it, effectSize = 0.56f)
        }
        sprites?.playerDestroyedFrames?.let {
            explosions += Explosion(player.position.x, player.position.y, it, effectSize = 0.78f)
        }
        notifyUi()
        if (session.state == GameState.GAME_OVER) finishGame()
    }

    private fun onEnemyKilled(enemy: Enemy) {
        val points = when (enemy.kind) {
            EnemyKind.NORMAL -> GameConfig.ENEMY_SCORE
            EnemyKind.ELITE -> GameConfig.ELITE_ENEMY_SCORE
            EnemyKind.BOSS -> GameConfig.BOSS_SCORE
        }
        session.addScore(points)
        sprites?.explosion?.let { explosions += Explosion(enemy.position.x, enemy.position.y, it) }

        if (Random.nextFloat() < GameConfig.ITEM_DROP_CHANCE) {
            val type = if (Random.nextBoolean()) ItemType.LIFE else ItemType.BOMB
            val region = if (type == ItemType.LIFE) itemSprites?.healthPickup else itemSprites?.bombPickup
            items += ItemPickup(enemy.position.x, enemy.position.y, type, region)
        }

        if (enemy.isBoss) {
            bullets.filter { it.owner == BulletOwner.ENEMY }.forEach { it.active = false }
            session.finishBoss()
            spawnTimer = 0f
            enemyShotTimer = 0f
        }
        debugPrint("score=${session.score}", display = false)
        notifyUi()
    }

    private fun finishGame() {
        if (gameOverSaved) return
        gameOverSaved = true
        leaderboard?.save(session.score)
        debugPrint("Game Over: score=${session.score}")
        notifyUi()
    }

    override fun draw(renderer: Renderer2D) {
        if (session.state == GameState.START || session.state == GameState.LEADERBOARD) return

        if (session.state != GameState.GAME_OVER && session.shouldDrawPlayer()) player.draw(renderer)
        bullets.filter { it.active }.forEach { it.draw(renderer) }
        enemies.filter { it.active }.forEach { it.draw(renderer) }
        items.filter { it.active }.forEach { it.draw(renderer) }
        explosions.filter { it.active }.forEach { it.draw(renderer) }
        screenFlashes.filter { it.active }.forEach { it.draw(renderer) }
        val boss = enemies.firstOrNull { it.active && it.isBoss }
        hud.draw(renderer, session, boss?.hp, boss?.let { GameConfig.bossMaxHp(session.cycle) })
    }

    private fun clearWorld() {
        bullets.clear()
        enemies.clear()
        items.clear()
        explosions.clear()
        screenFlashes.clear()
        shotTimer = 0f
        spawnTimer = 0f
        enemyShotTimer = 0f
    }

    private fun notifyUi() {
        uiListener?.onChanged(GameUiSnapshot(
            state = session.state,
            score = session.score,
            bombs = session.bombs,
            highScores = leaderboard?.scores().orEmpty(),
        ))
    }
}
