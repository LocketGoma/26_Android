package kr.ac.lecture.mobilegame.foundation.core

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.FitViewport
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.input.InputSnapshot
import kr.ac.lecture.mobilegame.foundation.scene.Scene
import kr.ac.lecture.mobilegame.foundation.scene.SceneManager
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** GL 스레드에서 입력 읽기 → 게임 계산 → 그리기를 반복합니다. 특정 게임의 규칙은 넣지 않습니다. */
class GameRenderer(
    context: Context,
    private val contentAspectRatio: Float?,
    nearestFiltering: Boolean,
    private val readInput: () -> InputSnapshot,
    private val createScene: (ResourceManager) -> Scene,
) : GLSurfaceView.Renderer {
    private val clock = GameClock()
    private val renderer = Renderer2D()
    private val resources = ResourceManager(context.applicationContext, nearestFiltering)
    private lateinit var scenes: SceneManager
    @Volatile private var paused = true
    @Volatile private var resetClock = true

    fun pause() { paused = true }

    fun resume() {
        resetClock = true
        paused = false
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        renderer.create()
        // GL 컨텍스트가 바뀌어도 게임 상태는 유지하고 GPU 그림만 다시 올립니다.
        if (::scenes.isInitialized) resources.reloadTextures()
        else scenes = SceneManager(createScene(resources))
        resetClock = true
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val area = contentAspectRatio?.let { FitViewport.fit(width, height, it) }
            ?: FitViewport(0, 0, width, height)
        GLES30.glViewport(area.left, height - area.top - area.height, area.width, area.height)
        renderer.setViewport(area.width, area.height)
        scenes.resize(area.width, area.height)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!::scenes.isInitialized) return
        // 앱 복귀 직후에는 쉬고 있던 시간을 이동 시간으로 더하지 않습니다.
        if (resetClock) {
            resetClock = false
            clock.reset()
        }
        val deltaTime = clock.tick()
        if (!paused) scenes.update(deltaTime, readInput())
        renderer.beginFrame()
        scenes.draw(renderer)
    }
}
