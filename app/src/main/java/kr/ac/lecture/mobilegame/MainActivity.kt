package kr.ac.lecture.mobilegame

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import kr.ac.lecture.mobilegame.foundation.core.GameSurfaceView
import kr.ac.lecture.mobilegame.foundation.debug.DebugRuntime
import kr.ac.lecture.mobilegame.foundation.audio.SoundManager
import kr.ac.lecture.mobilegame.game.ShooterScene
import kr.ac.lecture.mobilegame.game.sprite.ShootingSpriteCatalog

/** 사용할 게임과 그림·소리를 구성하고, Android의 시작·중지·종료를 연결합니다. */
class MainActivity : Activity() {
    private lateinit var gameView: GameSurfaceView
    private lateinit var sound: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugRuntime.initialize(applicationContext)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        sound = SoundManager(this)
        gameView = GameSurfaceView(this) { resources ->
            // 이 부분을 바꾸면 같은 기반 시스템으로 다른 Scene을 실행할 수 있습니다.
            val sprites = ShootingSpriteCatalog.load(resources)
            ShooterScene(sprites = sprites, sound = sound)
        }
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        sound.onHostResume()
        gameView.onHostResume()
    }

    override fun onPause() {
        gameView.onHostPause()
        sound.onHostPause()
        super.onPause()
    }

    override fun onDestroy() {
        sound.release()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && ::gameView.isInitialized) gameView.clearInput()
    }
}
