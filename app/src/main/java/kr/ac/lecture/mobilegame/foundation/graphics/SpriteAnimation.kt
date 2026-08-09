package kr.ac.lecture.mobilegame.foundation.graphics

/** 스프라이트 시트의 프레임 번호를 시간 기반으로 계산하는 Animation 초안입니다. */
class SpriteAnimation(
    private val frameCount: Int,
    private val secondsPerFrame: Float,
    private val looping: Boolean = true,
) {
    var currentFrame: Int = 0
        private set
    private var elapsed = 0f

    fun update(deltaTime: Float) {
        if (frameCount <= 1 || secondsPerFrame <= 0f) return
        elapsed += deltaTime
        while (elapsed >= secondsPerFrame) {
            elapsed -= secondsPerFrame
            currentFrame = if (looping) (currentFrame + 1) % frameCount
            else (currentFrame + 1).coerceAtMost(frameCount - 1)
        }
    }

    fun reset() { currentFrame = 0; elapsed = 0f }
}
