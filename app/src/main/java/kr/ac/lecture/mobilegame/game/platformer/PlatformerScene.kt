package kr.ac.lecture.mobilegame.game.platformer

import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.input.DigitalInput
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot
import kr.ac.lecture.mobilegame.foundation.scene.Scene

/** 입력 읽기, 게임 계산, Android HUD 알림, 그리기를 순서대로 연결합니다. */
class PlatformerScene(
    private val world: PlatformerWorld, private val controls: DigitalInput, private val painter: PlatformerPainter, private val onUiChanged: (PlatformerUiSnapshot) -> Unit
) : Scene {
    private var lastUi: PlatformerUiSnapshot? = null
    override fun onEnter() = notifyUi()

    override fun resize(width: Int, height: Int) = world.resizeViewport(width, height)

    override fun update(deltaTime: Float, input: InputSnapshot) {
        world.update(deltaTime, controls.drain())
        notifyUi()
    }

    override fun draw(renderer: Renderer2D) = painter.draw(renderer, world)

    private fun notifyUi() {
        val snapshot = world.uiSnapshot()
        if (snapshot != lastUi) {
            lastUi = snapshot
            onUiChanged(snapshot)
        }
    }
}
