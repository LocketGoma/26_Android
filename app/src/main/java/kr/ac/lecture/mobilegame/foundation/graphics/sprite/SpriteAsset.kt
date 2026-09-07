package kr.ac.lecture.mobilegame.foundation.graphics.sprite

import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager

/**
 * [강사 제공 기반 시스템] 이름·기본 Pixel Size·Frame 목록입니다. 재생 상태나 GL 소유권은 없습니다.
 * Size는 표시 크기이며 원본 Texture를 Resize하지 않습니다. Animation은 기존 SpriteAnimation을 재사용합니다.
 */
class SpriteAsset(
    val name: String,
    frames: List<SpriteRegion>,
    val widthPixels: Float,
    val heightPixels: Float,
    val secondsPerFrame: Float = 0.1f,
    val looping: Boolean = true,
) {
    val frames = frames.toList()
    init {
        require(name.isNotBlank())
        require(frames.size in 1..8) { "One animation asset must contain 1..8 frames" }
        require(widthPixels.isFinite() && widthPixels > 0f && heightPixels.isFinite() && heightPixels > 0f)
        require(secondsPerFrame.isFinite() && secondsPerFrame > 0f)
    }
}

/** Texture 생성이 필요하므로 ResourceManager와 동일한 GL Thread에서 호출합니다. */
class SpriteLoader(private val resources: ResourceManager) {
    fun loadSprite(
        resourceId: Int,
        name: String? = null,
        width: Float? = null,
        height: Float? = null,
        scale: Float = 1f,
    ): SpriteAsset {
        require((width == null) == (height == null)) { "Specify both width and height" }
        require(scale.isFinite() && scale > 0f)
        require(width == null || scale == 1f) { "Choose Pixel Size OR load scale" }
        require(width == null || (width.isFinite() && width > 0f && height!!.isFinite() && height > 0f))
        val texture = resources.texture(resourceId)
        return SpriteAsset(
            name ?: resources.resourceName(resourceId),
            listOf(SpriteRegion(texture, 0f, 0f, 1f, 1f)),
            width ?: texture.width * scale, height ?: texture.height * scale,
        )
    }
}
