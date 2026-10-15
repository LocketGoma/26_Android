package kr.ac.lecture.mobilegame.game

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteAsset
import kr.ac.lecture.mobilegame.foundation.component.SpriteComponent

/** 학생 수정 영역: 4프레임 효과를 재생한 뒤 스스로 비활성화하는 예제입니다. */
class Explosion(
    x: Float,
    y: Float,
    frames: List<SpriteRegion>,
    effectSize: Float = 0.34f,
) : GameObject(Vec2(x, y), Vec2(effectSize, effectSize)) {
    init {
        require(frames.isNotEmpty()) { "Explosion needs animation frames" }
        sprite = SpriteComponent(this, useObjectSize = true).apply {
            addSprite(SpriteAsset("Explosion", frames, frames.first().widthPixels, frames.first().heightPixels,
                secondsPerFrame = 0.08f, looping = false))
        }
        // 폭발은 시각 효과이므로 CollisionComponent를 붙이지 않습니다.
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        if (sprite?.isFinished == true) active = false
    }

    override fun draw(renderer: Renderer2D) {
        val current = sprite?.currentRegion ?: return
        val drawn = renderer.aspectCorrectedSize(current, size.x)
        drawn.x *= transform.scale.x
        drawn.y *= transform.scale.y
        renderer.drawSprite(current, position, drawn, rotation = transform.rotation)
    }
}
