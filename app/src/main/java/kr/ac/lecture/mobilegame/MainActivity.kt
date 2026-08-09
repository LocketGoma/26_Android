package kr.ac.lecture.mobilegame

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import kr.ac.lecture.mobilegame.foundation.core.GameSurfaceView
import kr.ac.lecture.mobilegame.foundation.debug.DebugRuntime

/** Android Lifecycle를 게임의 pause/resume과 연결하는 진입점입니다. */
class MainActivity : Activity() {
    private lateinit var gameView: GameSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugRuntime.initialize(applicationContext)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        gameView = GameSurfaceView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.onHostResume()
    }

    override fun onPause() {
        gameView.onHostPause()
        super.onPause()
    }

    override fun onDestroy() {
        gameView.onHostDestroy()
        super.onDestroy()
    }
}
