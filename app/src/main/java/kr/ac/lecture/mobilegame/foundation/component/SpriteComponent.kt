package kr.ac.lecture.mobilegame.foundation.component

import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.SpriteAnimation
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteAsset
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion

/**
 * 이미 읽은 그림 묶음(SpriteAsset)을 이름으로 선택합니다. 그림 파일을 직접 읽지 않습니다.
 * 현재 프레임은 객체마다 따로 계산하므로 같은 그림을 공유해도 재생 상태는 독립적입니다.
 * useObjectSize=true이면 그림의 픽셀 크기 대신 GameObject.size를 사용합니다.
 */
class SpriteComponent(private val owner: GameObject, private val useObjectSize: Boolean = false) {
    private val sprites = linkedMapOf<String, SpriteAsset>()
    private var current: SpriteAsset? = null
    private var animation: SpriteAnimation? = null
    var scale: Float = 1f
        private set

    fun addSprite(sprite: SpriteAsset): Boolean {
        if (sprites.containsKey(sprite.name)) return false
        sprites[sprite.name] = sprite
        if (current == null) setSprite(sprite.name)
        return true
    }

    /** 같은 이름은 현재 재생을 유지합니다. restart=true일 때만 처음부터 다시 재생합니다. */
    fun setSprite(name: String, restart: Boolean = false): Boolean {
        val next = sprites[name] ?: return false
        if (current === next && !restart) return true
        current = next
        animation = SpriteAnimation(next.frames.size, next.secondsPerFrame, next.looping)
        return true
    }

    fun getCurrentSprite(): SpriteAsset? = current
    val isFinished: Boolean get() = animation?.isFinished == true
    val currentRegion: SpriteRegion?
        get() = current?.frames?.get(animation?.currentFrame ?: 0)

    fun setScale(value: Float) {
        require(value.isFinite() && value >= 0f)
        scale = value
    }

    fun update(deltaTime: Float) { animation?.update(deltaTime) }

    fun draw(renderer: Renderer2D) {
        val asset = current ?: return
        val base = if (useObjectSize) owner.size else renderer.pixelSizeToWorld(asset.widthPixels, asset.heightPixels)
        val size = Vec2(base.x * owner.transform.scale.x * scale, base.y * owner.transform.scale.y * scale)
        renderer.drawSprite(currentRegion ?: return, owner.position, size, rotation = owner.transform.rotation)
    }
}
