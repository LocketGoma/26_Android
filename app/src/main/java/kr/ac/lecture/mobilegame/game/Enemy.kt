package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.game.sprite.SpriteClip
import kr.ac.lecture.mobilegame.foundation.component.SpriteComponent
import kr.ac.lecture.mobilegame.foundation.component.MovementComponent
import kr.ac.lecture.mobilegame.foundation.collision.AABBCollisionComponent

enum class EnemyKind { NORMAL, BOSS }

/** 학생 수정 영역: 적 이동 패턴과 난이도에 따라 speed를 확장하세요. */
class Enemy(
    x: Float,
    speed: Float = 0.45f,
    sprite: SpriteClip? = null,
    val kind: EnemyKind = EnemyKind.NORMAL,
    maxHp: Int = 1,
) : GameObject(
    Vec2(x, if (kind == EnemyKind.BOSS) 0.70f else 1.05f),
    if (kind == EnemyKind.BOSS) Vec2(0.62f, 0.46f) else Vec2(0.27f, 0.22f),
) {
    var hp: Int = maxHp
        private set
    val isBoss: Boolean get() = kind == EnemyKind.BOSS

    init {
        this.sprite = SpriteComponent(this, useObjectSize = true).apply {
            sprite?.let { addSprite(it.toAsset("Idle")) }
        }
        collision = AABBCollisionComponent(transform, size.copy())
        if (!isBoss) movement = MovementComponent(transform, maxSpeed = speed).apply {
            setMoveDirection(0f, -1f)
            velocity.y = -speed
        }
    }

    /** @return 이번 피해로 HP가 0이 되었으면 true */
    fun damage(amount: Int): Boolean {
        if (!active || amount <= 0) return false
        hp = (hp - amount).coerceAtLeast(0)
        if (hp == 0) active = false
        return hp == 0
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        if (!isBoss && position.y < -1.15f) active = false
    }
    override fun draw(renderer: Renderer2D) {
        if (sprite?.getCurrentSprite() != null) super.draw(renderer)
        else renderer.drawRect(position, Vec2(size.x * transform.scale.x, size.y * transform.scale.y),
            if (isBoss) Color(0.75f, 0.15f, 0.90f) else Color.RED, transform.rotation)
    }
}
