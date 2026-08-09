package kr.ac.lecture.mobilegame.foundation.scene

import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot

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
