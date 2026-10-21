package kr.ac.lecture.mobilegame

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import kr.ac.lecture.mobilegame.foundation.core.SceneSurfaceView
import kr.ac.lecture.mobilegame.foundation.core.DebugConfig
import kr.ac.lecture.mobilegame.foundation.input.DigitalInput
import kr.ac.lecture.mobilegame.game.addon.PlatformerWasdControls
import kr.ac.lecture.mobilegame.game.platformer.PlatformerControlsView
import kr.ac.lecture.mobilegame.game.platformer.PlatformerPainter
import kr.ac.lecture.mobilegame.game.platformer.PlatformerScene
import kr.ac.lecture.mobilegame.game.platformer.PlatformerSprites
import kr.ac.lecture.mobilegame.game.platformer.PlatformerState
import kr.ac.lecture.mobilegame.game.platformer.PlatformerUiSnapshot
import kr.ac.lecture.mobilegame.game.platformer.PlatformerWorld

/** 가로 전체 게임 화면 위에 HUD와 조작부를 겹쳐 그립니다. */
class PlatformerActivity : Activity() {
    private val controls = DigitalInput()
    private val keyboardControls = PlatformerWasdControls(controls)
    private val world = PlatformerWorld().apply {
        showCollision = BuildConfig.DEBUG_BLOCK && DebugConfig.SHOW_COLLIDER
    }
    private lateinit var gameView: SceneSurfaceView
    private lateinit var touchControls: PlatformerControlsView
    private lateinit var hud: TextView
    private lateinit var finishLabel: TextView
    private var followButton: Button? = null
    private var collisionButton: Button? = null
    private var gameplayEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        gameView = SceneSurfaceView(this) { resources ->
            PlatformerScene(world, controls, PlatformerPainter(PlatformerSprites(resources))) { snapshot ->
                runOnUiThread { if (!isFinishing && !isDestroyed) showStatus(snapshot) }
            }
        }
        setContentView(buildContentView())
        SampleScreen.hideSystemBars(this)
    }

    @Suppress("DEPRECATION")
    private fun buildContentView(): FrameLayout {
        val root = FrameLayout(this).apply { isMotionEventSplittingEnabled = true }
        root.addView(gameView, FrameLayout.LayoutParams(-1, -1))
        touchControls = PlatformerControlsView(this, controls)
        root.addView(touchControls, FrameLayout.LayoutParams(-1, -1))

        // 이 레이어의 여백만 조정합니다. 게임 화면은 시스템 바나 카메라 구멍 뒤까지 채웁니다.
        val overlay = FrameLayout(this).apply { isMotionEventSplittingEnabled = true }
        hud = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.WHITE)
            setShadowLayer(dp(2).toFloat(), 0f, dp(1).toFloat(), Color.DKGRAY)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        overlay.addView(hud, FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.START))
        if (BuildConfig.DEBUG_BLOCK) addDebugTools(overlay)
        finishLabel = TextView(this).apply {
            setText(R.string.platformer_clear)
            textSize = 32f
            setTextColor(Color.WHITE)
            setShadowLayer(dp(3).toFloat(), 0f, dp(2).toFloat(), Color.DKGRAY)
            setPadding(dp(16), dp(8), dp(16), dp(8))
            isFocusable = true
            setOnClickListener {
                touchControls.clearInputs()
                finish()
            }
            visibility = android.view.View.GONE
        }
        overlay.addView(finishLabel, FrameLayout.LayoutParams(-2, -2, Gravity.CENTER))
        SampleScreen.applyInsets(overlay)
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))
        root.setOnApplyWindowInsetsListener { _, insets ->
            touchControls.setSafeInsets(insets.systemWindowInsetLeft, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
        return root
    }

    /** 기존 DEBUG_BLOCK이 켜진 빌드에만 실습용 버튼을 만듭니다. */
    private fun addDebugTools(overlay: FrameLayout) {
        val tools = LinearLayout(this)
        SampleScreen.addDebugButton(tools, "↻", R.string.restart_game) {
            touchControls.clearInputs()
            gameView.runOnGameThread { world.restart() }
        }
        followButton = SampleScreen.addDebugButton(tools, "⇆", R.string.follow_platform_on) {
            gameView.runOnGameThread { world.followPlatform = !world.followPlatform }
        }
        collisionButton = SampleScreen.addDebugButton(tools, "□", R.string.show_collision) {
            gameView.runOnGameThread { world.showCollision = !world.showCollision }
        }
        overlay.addView(tools, FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.END))
    }

    private fun showStatus(snapshot: PlatformerUiSnapshot) {
        gameplayEnabled = snapshot.state == PlatformerState.PLAYING
        hud.text = "♥ ${snapshot.life}   ★ ${snapshot.score}   ◆ ${snapshot.collected}"
        followButton?.apply {
            setTextColor(if (snapshot.followPlatform) Color.rgb(180, 255, 155) else Color.WHITE)
            contentDescription = getString(if (snapshot.followPlatform) R.string.follow_platform_on else R.string.follow_platform_off)
        }
        collisionButton?.apply {
            setTextColor(if (snapshot.showCollision) Color.rgb(180, 255, 155) else Color.WHITE)
            contentDescription = getString(if (snapshot.showCollision) R.string.hide_collision else R.string.show_collision)
        }
        finishLabel.visibility = if (snapshot.state == PlatformerState.CLEARED) android.view.View.VISIBLE else android.view.View.GONE
        touchControls.setGameplayEnabled(gameplayEnabled)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return keyboardControls.onKey(event, gameplayEnabled) || super.dispatchKeyEvent(event)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        touchControls.clearInputs()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) SampleScreen.hideSystemBars(this) else if (::touchControls.isInitialized) touchControls.clearInputs()
    }

    override fun onResume() {
        super.onResume(); gameView.onHostResume()
    }

    override fun onPause() {
        touchControls.clearInputs(); gameView.onHostPause(); super.onPause()
    }
}
