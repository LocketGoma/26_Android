package kr.ac.lecture.mobilegame.game.sprite

import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteAsset

/**
 * [학생 실습/수정 영역]
 * 기반 시스템이 잘라 준 SpriteRegion과 재생 설정을 묶습니다. 재생 상태는 SpriteComponent만 가집니다.
 * 시트 전체 크기는 제한하지 않으며, 한 애니메이션 계열만 최대 8프레임으로 제한합니다.
 */
class SpriteClip(
    frames: List<SpriteRegion>,
    private val secondsPerFrame: Float,
    private val looping: Boolean = true,
) {
    val frames = frames.toList()
    init {
        require(frames.size in 1..MAX_FRAMES) {
            "One animation clip must contain 1..$MAX_FRAMES frames: ${frames.size}"
        }
        require(secondsPerFrame.isFinite() && secondsPerFrame > 0f)
    }

    /** 기존 Catalog의 Frame/재생 설정을 재사용합니다. Component는 별도 재생 상태를 만듭니다. */
    fun toAsset(name: String): SpriteAsset = SpriteAsset(
        name, frames,
        frames.first().widthPixels,
        frames.first().heightPixels,
        secondsPerFrame, looping,
    )

    companion object {
        const val MAX_FRAMES = 8
    }
}
