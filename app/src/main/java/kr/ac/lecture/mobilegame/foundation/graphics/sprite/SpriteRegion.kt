package kr.ac.lecture.mobilegame.foundation.graphics.sprite

import kr.ac.lecture.mobilegame.foundation.graphics.Texture

/** 전체 Texture 중 한 Sprite가 사용할 정규화 UV 영역입니다. */
data class SpriteRegion(
    val texture: Texture,
    val uLeft: Float,
    val vTop: Float,
    val uRight: Float,
    val vBottom: Float,
)
