package kr.ac.lecture.mobilegame.foundation.core

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import kr.ac.lecture.mobilegame.foundation.input.InputController
import kr.ac.lecture.mobilegame.foundation.input.SensorInput
import kr.ac.lecture.mobilegame.foundation.audio.SoundManager
import kr.ac.lecture.mobilegame.game.GameUiListener
import kr.ac.lecture.mobilegame.game.GameUiSnapshot

/** Activity와 OpenGL Render Thread 사이를 연결하는 기반 시스템 View입니다. */
@SuppressLint("ViewConstructor") // XML이 아니라 MainActivity에서 Callback과 함께 생성합니다.
class GameSurfaceView(
    context: Context,
    onUiChanged: (GameUiSnapshot) -> Unit,
    onExitRequested: () -> Unit,
) : GLSurfaceView(context) {
    private val input = InputController()
    private val sensorInput = SensorInput(context, input)
    private val sound = SoundManager(context)
    private val gameRenderer = GameRenderer(
        context,
        input,
        sound,
        GameUiListener { snapshot -> post { onUiChanged(snapshot) } },
        { post(onExitRequested) },
    )

    init {
        setEGLContextClientVersion(3)
        setRenderer(gameRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        setOnTouchListener { _, event -> input.onTouch(event, width, height) }
    }

    fun onHostResume() {
        super.onResume()
        sensorInput.start()
        sound.onHostResume()
        gameRenderer.resume()
    }

    fun onHostPause() {
        gameRenderer.pause()
        sound.onHostPause()
        sensorInput.stop()
        super.onPause()
    }

    fun onHostDestroy() {
        sensorInput.stop()
        sound.release()
        // OpenGL 객체 해제는 컨텍스트가 유효한 Render Thread에서 수행하도록 후속 실습에서 보강합니다.
    }

    fun startGame() = queueEvent(gameRenderer::startGame)
    fun openLeaderboard() = queueEvent(gameRenderer::openLeaderboard)
    fun closeLeaderboard() = queueEvent(gameRenderer::closeLeaderboard)
    fun useBomb() = queueEvent(gameRenderer::useBomb)
    fun exitGame() = queueEvent(gameRenderer::exitGame)
}
