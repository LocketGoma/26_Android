package kr.ac.lecture.mobilegame.foundation.ui

import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/** main에서 쓰는 간단한 HUD입니다. 점수·종료 표시를 바꾸고 그리는 기능만 둡니다. */
interface Hud {
    fun updateScore(score: Int)
    fun showGameOver(visible: Boolean)
    fun draw(renderer: Renderer2D)
}
