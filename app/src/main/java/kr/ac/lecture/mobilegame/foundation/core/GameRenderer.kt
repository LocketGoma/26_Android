package kr.ac.lecture.mobilegame.foundation.core

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D
import kr.ac.lecture.mobilegame.foundation.graphics.ResourceManager
import kr.ac.lecture.mobilegame.foundation.input.InputController
import kr.ac.lecture.mobilegame.foundation.scene.SceneManager
import kr.ac.lecture.mobilegame.game.ShooterScene
import kr.ac.lecture.mobilegame.game.sprite.ShootingSpriteCatalog
import kr.ac.lecture.mobilegame.foundation.audio.SoundManager
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** GLSurfaceView가 호출하는 Render Thread의 Game Loop 지점입니다. */
class GameRenderer(context: Context, private val input: InputController, private val sound: SoundManager) : GLSurfaceView.Renderer {
    private val applicationContext = context.applicationContext
    private val clock = GameClock()
    private val renderer = Renderer2D()
    private lateinit var resources: ResourceManager
    private lateinit var scenes: SceneManager
    @Volatile private var paused = true
    @Volatile private var resetClock = true

    fun pause() { paused = true }
    fun resume() { resetClock = true; paused = false }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        renderer.create()
        resources = ResourceManager(applicationContext)
        scenes = SceneManager(ShooterScene(ShootingSpriteCatalog.load(resources), sound))
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        if (width > 0 && height > 0) renderer.setViewport(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!::scenes.isInitialized) return
        // GameClock 자체는 GL Thread에서만 접근합니다.
        if (resetClock) { resetClock = false; clock.reset() }
        val deltaTime = clock.tick()
        if (!paused) scenes.update(deltaTime, input.snapshot())
        renderer.beginFrame()
        scenes.draw(renderer)
    }
}
