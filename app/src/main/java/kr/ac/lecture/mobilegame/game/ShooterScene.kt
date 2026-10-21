package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.BuildConfig
import kr.ac.lecture.mobilegame.foundation.audio.SoundManager
import kr.ac.lecture.mobilegame.foundation.audio.ToneEffect
import kr.ac.lecture.mobilegame.foundation.collision.CollisionSystem
import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.DebugConfig
import kr.ac.lecture.mobilegame.foundation.ui.DebugHud
import kr.ac.lecture.mobilegame.foundation.debug.debugPrint
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
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
    private val player: Player = Player(sprites?.newPlayerIdleClip()),
) : Scene {
    // 별도 음원 Asset이 없어도 ToneEffect로 피드백하며, 추후 SoundPool Asset으로 교체할 수 있습니다.
    private val session = GameSession()
    private val bullets = mutableListOf<Bullet>()
    private val enemies = mutableListOf<Enemy>()
    private val items = mutableListOf<ItemPickup>()
    private val explosions = mutableListOf<Explosion>()
    private val screenFlashes = mutableListOf<ScreenFlash>()
    private val hud = GameHud(uiSprites)
    private val debugHud = DebugHud()
    private val playerDebugGroup = listOf(player)
    private var shotTimer = 0f
    private var spawnTimer = 0f
    private var enemyShotTimer = 0f
    private var gameOverSaved = false
    private var highScores = leaderboard?.scores().orEmpty()
    private var lastUi: GameUiSnapshot? = null
    private var showCollision = BuildConfig.DEBUG_BLOCK && DebugConfig.SHOW_COLLIDER
    private var viewportHeight = 1
    private var hudTopInset = 0

    override fun onEnter() = notifyUi()

    override fun resize(width: Int, height: Int) {
        viewportHeight = height
        hud.setTopInset(hudTopInset, viewportHeight)
    }

    /** Android UI의 안전 여백을 HUD의 OpenGL 좌표에 맞춥니다. 게임 영역은 줄이지 않습니다. */
    fun setHudTopInset(pixels: Int) {
        hudTopInset = pixels.coerceAtLeast(0)
        hud.setTopInset(hudTopInset, viewportHeight)
    }

    fun toggleCollision() {
        if (!BuildConfig.DEBUG_BLOCK) return
        showCollision = !showCollision
        notifyUi()
    }

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
        screenFlashes += ScreenFlash(Color(1f, 0.72f, 0.15f, 0.46f), 0.34f).apply { active = true }
        spawnEffect(sprites?.explosion, player, 1.25f)
        clearEnemyBullets()
        // 피해 처리 중 아이템/효과는 추가해도 적 목록 자체는 바꾸지 않습니다.
        for (enemy in enemies) {
            if (!enemy.active) continue
            spawnEffect(sprites?.impact, enemy, if (enemy.isBoss) 0.58f else 0.34f)
            if (enemy.damage(GameConfig.BOMB_DAMAGE)) onEnemyKilled(enemy)
        }
        notifyUi()
    }

    override fun update(deltaTime: Float, input: InputSnapshot) {
        if (!session.isPlaying && session.state != GameState.GAME_OVER) return
        if (session.isPlaying) {
            if (session.update(deltaTime) && BuildConfig.SAMPLE_BLOCK) spawnBoss()
            // Input → 생성 규칙 → 이동 → 충돌 결과 순서입니다.
            player.readInput(input)
            player.update(deltaTime)
            if (BuildConfig.SAMPLE_BLOCK) updateShootingRules(deltaTime)
            bullets.updateActive(deltaTime)
            enemies.updateActive(deltaTime)
            items.updateActive(deltaTime)
        }
        // 마지막 피격으로 Game Over가 되어도 효과는 끝까지 재생합니다.
        explosions.updateActive(deltaTime)
        screenFlashes.updateActive(deltaTime)
        if (session.isPlaying) resolveCollisions()
        removeInactiveObjects()
        notifyUi()
    }

    private fun updateShootingRules(deltaTime: Float) {
        shotTimer += deltaTime
        while (shotTimer >= GameConfig.FIRE_INTERVAL_SECONDS) {
            shotTimer -= GameConfig.FIRE_INTERVAL_SECONDS
            bullets += Bullet(player.position.x, player.position.y + player.size.y, sprites?.playerBullet, BulletOwner.PLAYER).apply { active = true }
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
        val enemyFireInterval = if (session.state == GameState.BOSS) GameConfig.BOSS_FIRE_INTERVAL_SECONDS
        else GameConfig.ENEMY_FIRE_INTERVAL_SECONDS
        if (enemyShotTimer >= enemyFireInterval) {
            enemyShotTimer -= enemyFireInterval
            enemies.filter { it.active }.randomOrNull()?.let { enemy ->
                bullets += Bullet(enemy.position.x, enemy.position.y - enemy.size.y / 2f, sprites?.enemyBullet, BulletOwner.ENEMY).apply { active = true }
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
        enemies += Enemy(x = Random.nextFloat() * 1.7f - 0.85f, speed = (if (kind == EnemyKind.ELITE) 0.30f else 0.46f) + (session.cycle - 1) * 0.025f, sprite = clip, kind = kind, maxHp = if (kind == EnemyKind.ELITE) GameConfig.ELITE_ENEMY_HP else GameConfig.NORMAL_ENEMY_HP).apply { active = true }
    }

    private fun spawnBoss() {
        enemies.forEach { if (it.isBoss) it.active = false }
        enemies += Enemy(x = 0f, speed = 0f, sprite = sprites?.newBomberEnemyClip(), kind = EnemyKind.BOSS, maxHp = GameConfig.bossMaxHp(session.cycle)).apply { active = true }
        spawnTimer = 0f
        enemyShotTimer = 0f
    }

    private fun resolveCollisions() {
        resolvePlayerBullets()
        resolvePlayerDamage()
        if (session.isPlaying) collectItems()
    }

    private fun resolvePlayerBullets() {
        for (bullet in bullets) {
            if (!bullet.active || bullet.owner != BulletOwner.PLAYER) continue
            val enemy = enemies.firstOrNull { CollisionSystem.intersects(bullet, it) } ?: continue
            bullet.active = false
            spawnEffect(sprites?.impact, enemy, if (enemy.isBoss) 0.32f else 0.22f)
            if (enemy.damage(GameConfig.PLAYER_BULLET_DAMAGE)) onEnemyKilled(enemy)
        }
    }

    private fun resolvePlayerDamage() {
        for (bullet in bullets) {
            if (!bullet.active || bullet.owner != BulletOwner.ENEMY) continue
            if (CollisionSystem.intersects(player, bullet)) {
                bullet.active = false
                handlePlayerHit()
                if (!session.isPlaying) return
            }
        }

        enemies.firstOrNull { CollisionSystem.intersects(player, it) }?.let { enemy ->
            if (!enemy.isBoss && !session.isInvincible) enemy.active = false
            handlePlayerHit()
        }
    }

    private fun collectItems() {
        for (item in items) {
            if (!CollisionSystem.intersects(player, item)) continue
            item.active = false
            when (item.type) {
                ItemType.LIFE -> session.collectLife()
                ItemType.BOMB -> session.collectBomb()
            }
        }
    }

    private fun handlePlayerHit() {
        if (!session.takeHit()) return
        sound?.playTone(ToneEffect.HIT)
        screenFlashes += ScreenFlash(Color(1f, 0.08f, 0.04f, 0.52f), 0.28f).apply { active = true }
        spawnEffect(sprites?.impact, player, 0.68f)
        if (session.state == GameState.GAME_OVER) {
            spawnEffect(sprites?.playerDestroyedFrames, player, 0.78f)
            finishGame()
        } else spawnEffect(sprites?.playerHitFrames, player, 0.56f)
    }

    private fun onEnemyKilled(enemy: Enemy) {
        val points = when (enemy.kind) {
            EnemyKind.NORMAL -> GameConfig.ENEMY_SCORE
            EnemyKind.ELITE -> GameConfig.ELITE_ENEMY_SCORE
            EnemyKind.BOSS -> GameConfig.BOSS_SCORE
        }
        session.addScore(points)
        spawnEffect(sprites?.explosion, enemy)

        if (Random.nextFloat() < GameConfig.ITEM_DROP_CHANCE) {
            val type = if (Random.nextBoolean()) ItemType.LIFE else ItemType.BOMB
            val region = if (type == ItemType.LIFE) itemSprites?.healthPickup else itemSprites?.bombPickup
            items += ItemPickup(enemy.position.x, enemy.position.y, type, region).apply { active = true }
        }

        if (enemy.isBoss) {
            clearEnemyBullets()
            session.finishBoss()
            spawnTimer = 0f
            enemyShotTimer = 0f
        }
        debugPrint("score=${session.score}", display = false)
    }

    private fun finishGame() {
        if (gameOverSaved) return
        gameOverSaved = true
        highScores = leaderboard?.save(session.score).orEmpty()
        debugPrint("Game Over: score=${session.score}")
    }

    override fun draw(renderer: Renderer2D) {
        if (session.state == GameState.START || session.state == GameState.LEADERBOARD) return

        if (session.state != GameState.GAME_OVER && session.shouldDrawPlayer()) player.draw(renderer)
        bullets.drawActive(renderer)
        enemies.drawActive(renderer)
        items.drawActive(renderer)
        explosions.drawActive(renderer)
        screenFlashes.drawActive(renderer)
        val boss = enemies.firstOrNull { it.active && it.isBoss }
        if (BuildConfig.DEBUG_BLOCK && showCollision) {
            if (session.isPlaying) debugHud.draw(renderer, playerDebugGroup)
            debugHud.draw(renderer, bullets)
            debugHud.draw(renderer, enemies)
            debugHud.draw(renderer, items)
        }
        hud.draw(renderer, session)
        hud.drawBossHp(renderer, boss?.hp, boss?.maxHp)
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
        val snapshot = GameUiSnapshot(state = session.state, score = session.score, bombs = session.bombs, highScores = if (session.state == GameState.LEADERBOARD) highScores else emptyList(), showCollision = showCollision)
        if (snapshot != lastUi) {
            lastUi = snapshot
            uiListener?.onChanged(snapshot)
        }
    }

    private fun clearEnemyBullets() {
        bullets.forEach { if (it.owner == BulletOwner.ENEMY) it.active = false }
    }

    private fun spawnEffect(frames: List<SpriteRegion>?, target: GameObject, size: Float = 0.34f) {
        if (!frames.isNullOrEmpty()) {
            explosions += Explosion(target.position.x, target.position.y, frames, size).apply { active = true }
        }
    }

    private fun removeInactiveObjects() {
        bullets.removeAll { !it.active }
        enemies.removeAll { !it.active }
        items.removeAll { !it.active }
        explosions.removeAll { !it.active }
        screenFlashes.removeAll { !it.active }
    }

    private fun Iterable<GameObject>.updateActive(dt: Float) {
        for (obj in this) if (obj.active) obj.update(dt)
    }

    private fun Iterable<GameObject>.drawActive(renderer: Renderer2D) {
        for (obj in this) if (obj.active) obj.draw(renderer)
    }
}
