package kr.ac.lecture.mobilegame.foundation.scene

import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot

/** 한 게임 화면의 계산과 그리기입니다. 화면에 들어올 때와 나갈 때의 처리도 둘 수 있습니다. */
interface Scene {
    fun onEnter() = Unit
    fun resize(width: Int, height: Int) = Unit
    fun update(deltaTime: Float, input: InputSnapshot)
    fun draw(renderer: Renderer2D)
    fun onExit() = Unit
}

/** 현재 Scene을 갱신하고 그립니다. 전환할 때 종료 → 진입 → 화면 크기 전달 순서를 지킵니다. */
class SceneManager(initialScene: Scene) {
    private var current: Scene = initialScene.also { it.onEnter() }
    private var width = 0
    private var height = 0

    fun change(next: Scene) {
        current.onExit()
        current = next
        current.onEnter()
        if (width > 0 && height > 0) current.resize(width, height)
    }

    fun update(deltaTime: Float, input: InputSnapshot) = current.update(deltaTime, input)
    fun resize(width: Int, height: Int) {
        require(width > 0 && height > 0)
        this.width = width
        this.height = height
        current.resize(width, height)
    }

    fun draw(renderer: Renderer2D) = current.draw(renderer)
}
