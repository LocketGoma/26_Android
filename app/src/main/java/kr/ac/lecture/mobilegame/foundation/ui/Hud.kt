package kr.ac.lecture.mobilegame.foundation.ui

import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/** 전달받은 상태를 그리는 HUD입니다. 게임 상태를 직접 변경하지 않습니다. */
fun interface Hud<in T> {
    fun draw(renderer: Renderer2D, state: T)
}
