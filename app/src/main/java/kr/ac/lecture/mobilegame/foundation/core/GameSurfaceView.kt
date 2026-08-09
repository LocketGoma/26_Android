package kr.ac.lecture.mobilegame.foundation.core

import android.content.Context
import android.opengl.GLSurfaceView
import kr.ac.lecture.mobilegame.foundation.input.InputController
import kr.ac.lecture.mobilegame.foundation.input.SensorInput

/** Activity와 OpenGL Render Thread 사이를 연결하는 기반 시스템 View입니다. */
class GameSurfaceView(context: Context) : GLSurfaceView(context) {
    private val input = InputController()
    private val sensorInput = SensorInput(context, input)
    private val gameRenderer = GameRenderer(context, input)

    init {
        setEGLContextClientVersion(3)
        setRenderer(gameRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        setOnTouchListener { _, event -> input.onTouch(event, width, height) }
    }

    fun onHostResume() {
        super.onResume()
        sensorInput.start()
        gameRenderer.resume()
    }

    fun onHostPause() {
        gameRenderer.pause()
        sensorInput.stop()
        super.onPause()
    }

    fun onHostDestroy() {
        sensorInput.stop()
        // OpenGL 객체 해제는 컨텍스트가 유효한 Render Thread에서 수행하도록 후속 실습에서 보강합니다.
    }
}
