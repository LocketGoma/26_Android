package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteAsset
import kr.ac.lecture.mobilegame.foundation.component.SpriteComponent

/** 학생 수정 영역: 4프레임 효과를 재생한 뒤 스스로 비활성화하는 예제입니다. */
class Explosion(
    x: Float,
    y: Float,
    private val frames: List<SpriteRegion>,
    effectSize: Float = 0.34f,
) : GameObject(Vec2(x, y), Vec2(effectSize, effectSize)) {
    private val secondsPerFrame = 0.08f
    private var elapsed = 0f

    init {
        if (frames.isNotEmpty()) sprite = SpriteComponent(this, useObjectSize = true).apply {
            addSprite(SpriteAsset("Explosion", frames, frames.first().widthPixels, frames.first().heightPixels,
                secondsPerFrame, looping = false))
        }
        // 폭발은 시각 효과이므로 CollisionComponent를 붙이지 않습니다.
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        elapsed += deltaTime
        if (elapsed >= frames.size * secondsPerFrame) active = false
    }

    override fun draw(renderer: Renderer2D) {
        if (frames.isEmpty()) {
            renderer.drawRect(position, Vec2(size.x * transform.scale.x, size.x * transform.scale.x),
                Color.YELLOW, transform.rotation)
            return
        }
        val current = sprite?.currentRegion ?: return
        renderer.drawSprite(current, position,
            renderer.aspectCorrectedSize(current, size.x * transform.scale.x), rotation = transform.rotation)
    }
}
