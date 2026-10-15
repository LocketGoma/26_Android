package kr.ac.lecture.mobilegame.foundation.core

import android.annotation.SuppressLint
import android.content.Context
import kr.ac.lecture.mobilegame.foundation.input.InputController
import kr.ac.lecture.mobilegame.foundation.input.SensorInput
import kr.ac.lecture.mobilegame.foundation.graphics.FitViewport
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.scene.Scene

/** 공통 Scene View에 터치 좌표 변환과 센서 입력을 더합니다. 게임 규칙은 Scene이 담당합니다. */
@SuppressLint("ViewConstructor")
class GameSurfaceView(
    context: Context,
    private val contentAspectRatio: Float,
    createScene: (ResourceManager) -> Scene,
) : SceneSurfaceView(context, contentAspectRatio, nearestFiltering = false, createScene = createScene) {
    private val input = InputController()
    private val sensorInput = SensorInput(context, input)
    init {
        setOnTouchListener { _, event ->
            if (width > 0 && height > 0) input.onTouch(event, width, height,
                FitViewport.fit(width, height, contentAspectRatio)) else false
        }
    }

    override fun readInput() = input.snapshot()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clearInput()
    }

    override fun onHostResume() {
        sensorInput.start()
        super.onHostResume()
    }

    override fun onHostPause() {
        clearInput()
        sensorInput.stop()
        super.onHostPause()
    }

    fun clearInput() = input.clearTouch()
}
