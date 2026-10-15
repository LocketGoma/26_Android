package kr.ac.lecture.mobilegame.foundation.graphics

/** 화면 비율을 유지해 게임을 그릴 영역입니다. 터치 좌표도 같은 영역을 기준으로 바꾸며, 픽셀 원점은 화면 왼쪽 위입니다. */
data class FitViewport(val left: Int, val top: Int, val width: Int, val height: Int) {
    fun toNdc(pixelX: Float, pixelY: Float): Pair<Float, Float>? {
        if (pixelX < left || pixelX > left + width || pixelY < top || pixelY > top + height) return null
        return Pair((pixelX - left) / width * 2f - 1f, 1f - (pixelY - top) / height * 2f)
    }

    companion object {
        fun fit(width: Int, height: Int, aspect: Float): FitViewport {
            require(width > 0 && height > 0 && aspect > 0f)
            val fittedWidth = minOf(width, (height * aspect).toInt()).coerceAtLeast(1)
            val fittedHeight = minOf(height, (fittedWidth / aspect).toInt()).coerceAtLeast(1)
            return FitViewport((width - fittedWidth) / 2, (height - fittedHeight) / 2, fittedWidth, fittedHeight)
        }
    }
}
