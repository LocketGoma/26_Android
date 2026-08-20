package kr.ac.lecture.mobilegame.foundation.core

/** Frame 사이의 DeltaTime을 초 단위로 계산합니다. 긴 일시정지 뒤의 급격한 이동은 최댓값 제한으로 방지합니다. */
class GameClock(private val maximumDeltaSeconds: Float = 0.05f) {
    private var previousNanos = 0L

    fun reset() { previousNanos = 0L }

    fun tick(nowNanos: Long = System.nanoTime()): Float {
        if (previousNanos == 0L) {
            previousNanos = nowNanos
            return 0f
        }
        val delta = (nowNanos - previousNanos) / 1_000_000_000f
        previousNanos = nowNanos
        return delta.coerceIn(0f, maximumDeltaSeconds)
    }
}
