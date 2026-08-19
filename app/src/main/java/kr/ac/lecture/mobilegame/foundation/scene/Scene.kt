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

/**
 * 현재 Scene의 수명과 전환을 관리하는 Scene Manager입니다.
 * Scene Interface와 전환 순서를 한곳에서 읽을 수 있도록 같은 파일에 둡니다.
 */
class SceneManager(initialScene: Scene) {
    private var current: Scene = initialScene.also { it.onEnter() }

    fun change(next: Scene) {
        current.onExit()
        current = next
        current.onEnter()
    }

    fun update(deltaTime: Float, input: InputSnapshot) = current.update(deltaTime, input)
    fun draw(renderer: Renderer2D) = current.draw(renderer)
}
