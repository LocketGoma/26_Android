package kr.ac.lecture.mobilegame.foundation.core

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot
import kr.ac.lecture.mobilegame.foundation.scene.Scene

/** 게임 규칙을 넣지 않고 Scene의 Update/Render와 화면 크기 변경을 연결합니다. */
@SuppressLint("ViewConstructor")
open class SceneSurfaceView(
    context: Context,
    contentAspectRatio: Float? = null,
    nearestFiltering: Boolean = true,
    createScene: (ResourceManager) -> Scene,
) : GLSurfaceView(context) {
    private val driver = GameRenderer(context, contentAspectRatio, nearestFiltering, ::readInput, createScene)

    init {
        setEGLContextClientVersion(3)
        preserveEGLContextOnPause = true
        setRenderer(driver)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    protected open fun readInput() = InputSnapshot()

    open fun onHostResume() {
        driver.resume(); super.onResume()
    }

    open fun onHostPause() {
        driver.pause(); super.onPause()
    }

    fun runOnGameThread(action: () -> Unit) = queueEvent { action() }
}
