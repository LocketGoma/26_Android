package kr.ac.lecture.mobilegame.game.sprite

import kr.ac.lecture.mobilegame.foundation.graphics.Texture
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import org.junit.Assert.assertThrows
import org.junit.Test

class SpriteClipTest {
    private val frame = SpriteRegion(Texture(1, 32, 32), 0f, 0f, 1f, 1f)

    @Test fun oneAnimationSeriesCannotExceedEightFrames() {
        assertThrows(IllegalArgumentException::class.java) {
            SpriteClip(List(9) { frame }, secondsPerFrame = 0.1f)
        }
    }
}
