package kr.ac.lecture.mobilegame.game.sprite

import kr.ac.lecture.mobilegame.foundation.graphics.SpriteAnimation
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion

/**
 * [학생 실습/수정 영역]
 * 기반 시스템이 잘라 준 SpriteRegion을 의미 있는 애니메이션 계열로 묶습니다.
 * 시트 전체 크기는 제한하지 않으며, 한 애니메이션 계열만 최대 8프레임으로 제한합니다.
 */
class SpriteClip(
    val frames: List<SpriteRegion>,
    secondsPerFrame: Float,
    looping: Boolean = true,
) {
    init {
        require(frames.size in 1..MAX_FRAMES) {
            "One animation clip must contain 1..$MAX_FRAMES frames: ${frames.size}"
        }
    }

    private val animation = SpriteAnimation(frames.size, secondsPerFrame, looping)
    val current: SpriteRegion get() = frames[animation.currentFrame]

    fun update(deltaTime: Float) = animation.update(deltaTime)
    fun reset() = animation.reset()

    companion object {
        const val MAX_FRAMES = 8
    }
}
