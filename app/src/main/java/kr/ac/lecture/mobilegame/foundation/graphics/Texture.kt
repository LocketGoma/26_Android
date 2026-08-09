package kr.ac.lecture.mobilegame.foundation.graphics

/** GPU에 한 번 올라간 전체 이미지. Sprite들은 이 Texture와 서로 다른 UV 영역을 공유합니다. */
data class Texture(val handle: Int, val width: Int, val height: Int)
