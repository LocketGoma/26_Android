package kr.ac.lecture.mobilegame.foundation.ui

import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/** 점수와 GameOver를 표현할 UI 경계. 텍스트 렌더링은 후속 실습에서 구현합니다. */
interface Hud {
    fun updateScore(score: Int)
    fun showGameOver(visible: Boolean)
    fun draw(renderer: Renderer2D)
}
