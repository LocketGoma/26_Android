package kr.ac.lecture.mobilegame.foundation.ui

import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/** 점수와 GameOver 표시 기능을 정의하는 UI Interface입니다. Text Rendering은 후속 실습에서 구현합니다. */
interface Hud {
    fun updateScore(score: Int)
    fun showGameOver(visible: Boolean)
    fun draw(renderer: Renderer2D)
}
