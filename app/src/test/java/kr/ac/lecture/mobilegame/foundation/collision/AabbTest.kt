package kr.ac.lecture.mobilegame.foundation.collision

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AabbTest {
    @Test fun overlappingBoxesCollide() {
        assertTrue(Aabb(0f, 0f, 2f, 2f).overlaps(Aabb(1f, 1f, 3f, 3f)))
    }

    @Test fun separatedBoxesDoNotCollide() {
        assertFalse(Aabb(0f, 0f, 1f, 1f).overlaps(Aabb(2f, 2f, 3f, 3f)))
    }

    @Test fun touchingEdgesDoNotCountAsOverlap() {
        assertFalse(Aabb(0f, 0f, 1f, 1f).overlaps(Aabb(1f, 0f, 2f, 1f)))
    }
}
