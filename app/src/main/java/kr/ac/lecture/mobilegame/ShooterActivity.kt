package kr.ac.lecture.mobilegame

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kr.ac.lecture.mobilegame.foundation.core.GameSurfaceView
import kr.ac.lecture.mobilegame.foundation.audio.SoundManager
import kr.ac.lecture.mobilegame.foundation.debug.DebugRuntime
import kr.ac.lecture.mobilegame.foundation.graphics.FitViewport
import kr.ac.lecture.mobilegame.game.GameState
import kr.ac.lecture.mobilegame.game.GameUiSnapshot
import kr.ac.lecture.mobilegame.game.GameUiListener
import kr.ac.lecture.mobilegame.game.LeaderboardStore
import kr.ac.lecture.mobilegame.game.ShooterScene
import kr.ac.lecture.mobilegame.game.sprite.ShootingSpriteCatalog
import kr.ac.lecture.mobilegame.game.sprite.ItemSpriteCatalog
import kr.ac.lecture.mobilegame.game.sprite.UiSpriteCatalog

/** Android Lifecycle와 네이티브 메뉴 UI를 OpenGL 게임에 연결하는 진입점입니다. */
class ShooterActivity : Activity() {
    private lateinit var gameView: GameSurfaceView
    private lateinit var scene: ShooterScene // 생성과 게임 명령은 GL 스레드에서만 처리합니다.
    private lateinit var sound: SoundManager
    private lateinit var menuOverlay: LinearLayout
    private lateinit var menuScroll: ScrollView
    private lateinit var titleView: TextView
    private lateinit var primaryButton: Button
    private lateinit var secondaryButton: Button
    private lateinit var tertiaryButton: Button
    private lateinit var bombTouchArea: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugRuntime.initialize(applicationContext)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        SampleScreen.hideSystemBars(this)

        sound = SoundManager(this)
        gameView = GameSurfaceView(this, CONTENT_ASPECT_RATIO) { resources ->
            ShooterScene(
                ShootingSpriteCatalog.load(resources), ItemSpriteCatalog.load(resources),
                UiSpriteCatalog.load(resources), LeaderboardStore(applicationContext),
                GameUiListener { snapshot ->
                    runOnUiThread { if (!isFinishing && !isDestroyed) showGameUi(snapshot) }
                }, sound,
            ).also { scene = it }
        }
        setContentView(buildContentView())
    }

    private fun buildContentView(): View {
        val root = FrameLayout(this)
        root.addView(gameView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        ))

        menuOverlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(32), dp(28), dp(32))
            setBackgroundColor(Color.argb(225, 8, 14, 32))
        }
        titleView = TextView(this).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 28f
            setPadding(0, 0, 0, dp(24))
        }
        primaryButton = menuButton()
        secondaryButton = menuButton()
        tertiaryButton = menuButton()
        menuOverlay.addView(titleView, menuItemParams())
        menuOverlay.addView(primaryButton, menuItemParams())
        menuOverlay.addView(secondaryButton, menuItemParams())
        menuOverlay.addView(tertiaryButton, menuItemParams())
        menuScroll = ScrollView(this).apply {
            isFillViewport = true
            addView(menuOverlay)
        }
        root.addView(menuScroll, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        ))

        // 실제 아이콘과 수량은 기존 ui_sprites.png로 그립니다. 이 투명 View는 터치와 접근성만 담당합니다.
        bombTouchArea = Button(this).apply {
            text = ""
            contentDescription = getString(R.string.bomb_button)
            setBackgroundColor(Color.TRANSPARENT)
            visibility = View.GONE
            setOnClickListener { command { useBomb() } }
        }
        root.addView(bombTouchArea, FrameLayout.LayoutParams(dp(136), dp(88), Gravity.TOP or Gravity.END))
        gameView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (gameView.width > 0 && gameView.height > 0) {
                val area = FitViewport.fit(gameView.width, gameView.height, CONTENT_ASPECT_RATIO)
                bombTouchArea.layoutParams = FrameLayout.LayoutParams(
                    (area.width * 0.3f).toInt().coerceAtLeast(1),
                    (area.height * 0.14f).toInt().coerceAtLeast(1),
                    Gravity.TOP or Gravity.START,
                ).apply {
                    leftMargin = area.left + (area.width * 0.7f).toInt()
                    topMargin = area.top
                }
            }
        }
        SampleScreen.applyInsets(root)
        return root
    }

    private fun menuButton() = Button(this).apply {
        isAllCaps = false
        textSize = 18f
    }

    private fun menuItemParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
    ).apply { setMargins(0, dp(6), 0, dp(6)) }

    private fun showGameUi(snapshot: GameUiSnapshot) {
        bombTouchArea.contentDescription = getString(R.string.bomb_count_description, snapshot.bombs)
        when (snapshot.state) {
            GameState.START -> showStartMenu()
            GameState.NORMAL, GameState.BOSS -> {
                menuScroll.visibility = View.GONE
                bombTouchArea.visibility = View.VISIBLE
                bombTouchArea.isEnabled = snapshot.bombs > 0
            }
            GameState.GAME_OVER -> showGameOverMenu(snapshot.score)
            GameState.LEADERBOARD -> showLeaderboard(snapshot.highScores)
        }
    }

    private fun showStartMenu() {
        menuScroll.visibility = View.VISIBLE
        bombTouchArea.visibility = View.GONE
        titleView.text = getString(R.string.game_title)
        setButton(primaryButton, R.string.start_game) { command { startGame() } }
        setButton(secondaryButton, R.string.show_leaderboard) { command { openLeaderboard() } }
        tertiaryButton.visibility = View.GONE
    }

    private fun showGameOverMenu(score: Int) {
        menuScroll.visibility = View.VISIBLE
        bombTouchArea.visibility = View.GONE
        titleView.text = getString(R.string.game_over_score, score)
        setButton(primaryButton, R.string.leaderboard) { command { openLeaderboard() } }
        setButton(secondaryButton, R.string.restart_game) { command { startGame() } }
        setButton(tertiaryButton, R.string.exit_game, ::finish)
    }

    private fun showLeaderboard(scores: List<Int>) {
        menuScroll.visibility = View.VISIBLE
        bombTouchArea.visibility = View.GONE
        val rows = if (scores.isEmpty()) getString(R.string.no_scores) else
            scores.mapIndexed { index, score -> getString(R.string.leaderboard_row, index + 1, score) }
                .joinToString("\n")
        titleView.text = getString(R.string.leaderboard_contents, getString(R.string.leaderboard), rows)
        setButton(primaryButton, R.string.back) { command { closeLeaderboard() } }
        secondaryButton.visibility = View.GONE
        tertiaryButton.visibility = View.GONE
    }

    private fun setButton(button: Button, textId: Int, action: () -> Unit) {
        button.visibility = View.VISIBLE
        button.setText(textId)
        button.setOnClickListener { action() }
    }

    private fun command(action: ShooterScene.() -> Unit) = gameView.runOnGameThread {
        if (::scene.isInitialized) scene.action()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) SampleScreen.hideSystemBars(this) else if (::gameView.isInitialized) gameView.clearInput()
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

    companion object { private const val CONTENT_ASPECT_RATIO = 9f / 16f }
}
