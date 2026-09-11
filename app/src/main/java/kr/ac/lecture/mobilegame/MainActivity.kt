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
import android.widget.TextView
import kr.ac.lecture.mobilegame.foundation.core.GameSurfaceView
import kr.ac.lecture.mobilegame.foundation.debug.DebugRuntime
import kr.ac.lecture.mobilegame.game.GameState
import kr.ac.lecture.mobilegame.game.GameUiSnapshot

/** Android Lifecycle와 네이티브 메뉴 UI를 OpenGL 게임에 연결하는 진입점입니다. */
class MainActivity : Activity() {
    private lateinit var gameView: GameSurfaceView
    private lateinit var menuOverlay: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var primaryButton: Button
    private lateinit var secondaryButton: Button
    private lateinit var tertiaryButton: Button
    private lateinit var bombTouchArea: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugRuntime.initialize(applicationContext)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        gameView = GameSurfaceView(this, ::showGameUi, ::finish)
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
        root.addView(menuOverlay, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        ))

        // 실제 아이콘과 수량은 기존 ui_sprites.png로 그립니다. 이 투명 View는 터치와 접근성만 담당합니다.
        bombTouchArea = Button(this).apply {
            text = ""
            contentDescription = getString(R.string.bomb_button)
            setBackgroundColor(Color.TRANSPARENT)
            visibility = View.GONE
            setOnClickListener { gameView.useBomb() }
        }
        root.addView(bombTouchArea, FrameLayout.LayoutParams(dp(136), dp(88), Gravity.TOP or Gravity.END))
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
                menuOverlay.visibility = View.GONE
                bombTouchArea.visibility = View.VISIBLE
                bombTouchArea.isEnabled = snapshot.bombs > 0
            }
            GameState.GAME_OVER -> showGameOverMenu(snapshot.score)
            GameState.LEADERBOARD -> showLeaderboard(snapshot.highScores)
        }
    }

    private fun showStartMenu() {
        menuOverlay.visibility = View.VISIBLE
        bombTouchArea.visibility = View.GONE
        titleView.text = getString(R.string.game_title)
        setButton(primaryButton, R.string.start_game) { gameView.startGame() }
        setButton(secondaryButton, R.string.show_leaderboard) { gameView.openLeaderboard() }
        tertiaryButton.visibility = View.GONE
    }

    private fun showGameOverMenu(score: Int) {
        menuOverlay.visibility = View.VISIBLE
        bombTouchArea.visibility = View.GONE
        titleView.text = getString(R.string.game_over_score, score)
        setButton(primaryButton, R.string.leaderboard) { gameView.openLeaderboard() }
        setButton(secondaryButton, R.string.restart_game) { gameView.startGame() }
        setButton(tertiaryButton, R.string.exit_game) { gameView.exitGame() }
    }

    private fun showLeaderboard(scores: List<Int>) {
        menuOverlay.visibility = View.VISIBLE
        bombTouchArea.visibility = View.GONE
        val rows = if (scores.isEmpty()) getString(R.string.no_scores) else
            scores.mapIndexed { index, score -> getString(R.string.leaderboard_row, index + 1, score) }
                .joinToString("\n")
        titleView.text = getString(R.string.leaderboard_contents, getString(R.string.leaderboard), rows)
        setButton(primaryButton, R.string.back) { gameView.closeLeaderboard() }
        secondaryButton.visibility = View.GONE
        tertiaryButton.visibility = View.GONE
    }

    private fun setButton(button: Button, textId: Int, action: () -> Unit) {
        button.visibility = View.VISIBLE
        button.setText(textId)
        button.setOnClickListener { action() }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

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
