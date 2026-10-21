package kr.ac.lecture.mobilegame.foundation.core

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent
import kr.ac.lecture.mobilegame.foundation.graphics.FitViewport
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.input.InputController
import kr.ac.lecture.mobilegame.foundation.input.SensorInput
import kr.ac.lecture.mobilegame.foundation.scene.Scene

/** Android의 화면·터치·키보드·센서를 게임 루프에 연결합니다. */
@SuppressLint("ViewConstructor")
class GameSurfaceView(
    context: Context,
    private val contentAspectRatio: Float? = null,
    nearestFiltering: Boolean = false,
    private val input: InputController = InputController(),
    createScene: (ResourceManager) -> Scene,
) : SceneSurfaceView(context, contentAspectRatio, nearestFiltering, createScene) {
    private val sensorInput = SensorInput(context, input)

    init {
        setOnTouchListener { _, event ->
            if (width <= 0 || height <= 0) false
            else {
                // 그리는 영역과 같은 영역으로 터치 위치를 변환합니다.
                val area = contentAspectRatio?.let { FitViewport.fit(width, height, it) } ?: FitViewport(0, 0, width, height)
                input.onTouch(event, width, height, area)
            }
        }
    }

    override fun readInput() = input.snapshot()

    override fun onHostResume() {
        sensorInput.start()
        super.onHostResume()
    }

    override fun onHostPause() {
        clearInput()
        sensorInput.stop()
        super.onHostPause()
    }

    fun handleKeyEvent(event: KeyEvent) = input.onKey(event)

    fun clearInput() = input.clear()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clearInput()
    }
}
