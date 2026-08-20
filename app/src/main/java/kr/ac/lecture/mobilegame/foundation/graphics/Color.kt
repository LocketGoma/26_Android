package kr.ac.lecture.mobilegame.foundation.graphics

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float = 1f) {
    companion object {
        val CYAN = Color(0.20f, 0.80f, 1.00f)
        val RED = Color(1.00f, 0.25f, 0.30f)
        val YELLOW = Color(1.00f, 0.85f, 0.20f)
        val WHITE = Color(1f, 1f, 1f)
        val BLACK = Color(0.0f, 0.0f, 0.0f)
    }
}
