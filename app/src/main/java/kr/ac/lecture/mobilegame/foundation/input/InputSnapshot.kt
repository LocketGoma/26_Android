package kr.ac.lecture.mobilegame.foundation.input

data class InputSnapshot(
    val touching: Boolean = false,
    val worldX: Float = 0f,
    val worldY: Float = 0f,
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val gyroscopeZ: Float = 0f,
)
