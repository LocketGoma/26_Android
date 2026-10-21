package kr.ac.lecture.mobilegame.foundation.graphics

/** 스프라이트 프레임과 일회 재생의 종료를 계산합니다. 마지막 프레임도 지정 시간만큼 유지합니다. */
class SpriteAnimation(
    private val frameCount: Int,
    private val secondsPerFrame: Float,
    private val looping: Boolean = true,
) {
    var currentFrame: Int = 0
        private set
    private var elapsed = 0f
    var isFinished = false
        private set

    init {
        require(frameCount > 0)
        require(secondsPerFrame.isFinite() && secondsPerFrame > 0f)
    }

    fun update(deltaTime: Float) {
        require(deltaTime.isFinite() && deltaTime >= 0f)
        if (isFinished) return
        elapsed += deltaTime
        while (elapsed >= secondsPerFrame) {
            elapsed -= secondsPerFrame
            if (!looping && currentFrame == frameCount - 1) {
                isFinished = true
                elapsed = 0f
                break
            }
            currentFrame = (currentFrame + 1) % frameCount
        }
    }

    fun reset() {
        currentFrame = 0; elapsed = 0f; isFinished = false
    }
}
