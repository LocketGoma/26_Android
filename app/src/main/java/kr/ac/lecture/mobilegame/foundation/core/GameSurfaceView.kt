package kr.ac.lecture.mobilegame.foundation.core

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import kr.ac.lecture.mobilegame.foundation.graphics.FitViewport
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.input.InputController
import kr.ac.lecture.mobilegame.foundation.input.SensorInput
import kr.ac.lecture.mobilegame.foundation.scene.Scene

/** Android의 화면·터치·센서를 게임 루프에 연결합니다. 어떤 게임을 만들지는 Activity에서 정합니다. */
@SuppressLint("ViewConstructor")
class GameSurfaceView(
    context: Context,
    private val contentAspectRatio: Float? = null,
    nearestFiltering: Boolean = false,
    createScene: (ResourceManager) -> Scene,
) : GLSurfaceView(context) {
    private val input = InputController()
    private val sensorInput = SensorInput(context, input)
    private val gameRenderer = GameRenderer(
        context = context,
        contentAspectRatio = contentAspectRatio,
        nearestFiltering = nearestFiltering,
        readInput = input::snapshot,
        createScene = createScene,
    )

    init {
        setEGLContextClientVersion(3)
        preserveEGLContextOnPause = true
        setRenderer(gameRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        setOnTouchListener { _, event ->
            if (width <= 0 || height <= 0) false
            else {
                // 그리는 영역과 같은 영역으로 터치 위치를 변환합니다.
                val area = contentAspectRatio?.let { FitViewport.fit(width, height, it) }
                    ?: FitViewport(0, 0, width, height)
                input.onTouch(event, width, height, area)
            }
        }
    }

    fun onHostResume() {
        sensorInput.start()
        gameRenderer.resume()
        super.onResume()
    }

    fun onHostPause() {
        clearInput()
        sensorInput.stop()
        gameRenderer.pause()
        super.onPause()
    }

    fun clearInput() = input.clearTouch()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clearInput()
    }
}
