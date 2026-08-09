package kr.ac.lecture.mobilegame.foundation.scene

import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot

/** Unity Scene / Unreal Level에 대응하는, 한 화면의 상태와 규칙입니다. */
interface Scene {
    fun onEnter() = Unit
    fun update(deltaTime: Float, input: InputSnapshot)
    fun draw(renderer: Renderer2D)
    fun onExit() = Unit
}
