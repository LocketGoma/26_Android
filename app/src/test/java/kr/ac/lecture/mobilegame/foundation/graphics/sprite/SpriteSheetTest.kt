package kr.ac.lecture.mobilegame.foundation.graphics.sprite

import kr.ac.lecture.mobilegame.foundation.graphics.Texture
import org.junit.Assert.assertEquals
import org.junit.Test

class SpriteSheetTest {
    private val sheet = SpriteSheet(Texture(handle = 1, width = 400, height = 200), columns = 4, rows = 2)

    @Test fun gridCreatesExpectedFrameCount() {
        assertEquals(8, sheet.frameCount)
    }

    @Test fun regionCalculatesUvAndHalfTexelInset() {
        val region = sheet.region(column = 1, row = 0)
        assertEquals(0.25125f, region.uLeft, 0.00001f)
        assertEquals(0.49875f, region.uRight, 0.00001f)
        assertEquals(0.0025f, region.vTop, 0.00001f)
        assertEquals(0.4975f, region.vBottom, 0.00001f)
    }
}
