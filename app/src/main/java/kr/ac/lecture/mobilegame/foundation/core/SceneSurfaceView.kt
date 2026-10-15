package kr.ac.lecture.mobilegame.foundation.core

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot
import kr.ac.lecture.mobilegame.foundation.scene.Scene
import kr.ac.lecture.mobilegame.foundation.scene.SceneManager
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** 게임 규칙을 넣지 않고 Scene의 Update/Render와 화면 크기 변경을 연결합니다. */
@SuppressLint("ViewConstructor")
class SceneSurfaceView(
    context: Context,
    private val createScene: (ResourceManager) -> Scene,
) : GLSurfaceView(context) {
    @Volatile private var paused = true
    @Volatile private var resetClock = true

    init {
        setEGLContextClientVersion(3)
        preserveEGLContextOnPause = true
        setRenderer(Driver())
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun onHostResume() { resetClock = true; paused = false; super.onResume() }
    fun onHostPause() { paused = true; super.onPause() }
    fun runOnGameThread(action: () -> Unit) = queueEvent { action() }

    private inner class Driver : GLSurfaceView.Renderer {
        private val clock = GameClock()
        private val renderer = Renderer2D()
        private lateinit var scenes: SceneManager

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            renderer.create()
            // 새 GL 컨텍스트에서는 Texture를 다시 읽습니다. 게임 상태는 Activity의 모델에 남습니다.
            val resources = ResourceManager(context.applicationContext, nearestFiltering = true)
            scenes = SceneManager(createScene(resources))
            resetClock = true
            GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            if (width <= 0 || height <= 0) return
            GLES30.glViewport(0, 0, width, height)
            renderer.setViewport(width, height)
            scenes.resize(width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            if (!::scenes.isInitialized) return
            if (resetClock) { resetClock = false; clock.reset() }
            val dt = clock.tick()
            if (!paused) scenes.update(dt, InputSnapshot())
            renderer.beginFrame()
            scenes.draw(renderer)
        }
    }
}
