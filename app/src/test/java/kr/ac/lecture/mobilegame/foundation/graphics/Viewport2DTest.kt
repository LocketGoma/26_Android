package kr.ac.lecture.mobilegame.foundation.graphics

import org.junit.Assert.assertEquals
import org.junit.Test

class Viewport2DTest {
    @Test fun squareSpriteKeepsSquarePixelSizeOnPortraitScreen() {
        val viewport = Viewport2D().apply { resize(1080, 2400) }

        val size = viewport.aspectCorrectedSize(worldWidth = 0.14f, widthToHeight = 1f)

        assertEquals(75.6f, size.x * 1080f / 2f, 0.001f)
        assertEquals(75.6f, size.y * 2400f / 2f, 0.001f)
    }
}
