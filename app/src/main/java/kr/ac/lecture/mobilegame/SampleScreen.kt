package kr.ac.lecture.mobilegame

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout

/** 게임 선택 화면과 두 게임에서 함께 쓰는 Android 화면 설정입니다. */
object SampleScreen {
    /** 두 게임의 실습 도구를 같은 크기와 모양으로 만듭니다. 동작은 각 Activity에서 연결합니다. */
    fun addDebugButton(tools: LinearLayout, symbol: String, description: Int, action: () -> Unit): Button {
        val context = tools.context
        return Button(context).apply {
            text = symbol
            contentDescription = context.getString(description)
            textSize = 22f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 0)
            minWidth = 0
            minHeight = 0
            stateListAnimator = null
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(100, 20, 35, 25))
            }
            setOnClickListener { action() }
            tools.addView(this, LinearLayout.LayoutParams(context.dp(44), context.dp(44)).apply {
                setMargins(context.dp(3), context.dp(3), context.dp(3), context.dp(3))
            })
        }
    }

    @Suppress("DEPRECATION")
    fun hideSystemBars(activity: Activity) {
        val window = activity.window
        val decorView = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            // 화면이 연결된 뒤 View에서 가져옵니다. 초기 Window getter는 내부 DecorView가 없으면 실패할 수 있습니다.
            decorView.post {
                if (activity.isFinishing || activity.isDestroyed) return@post
                decorView.windowInsetsController?.apply {
                    hide(WindowInsets.Type.systemBars())
                    systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    @Suppress("DEPRECATION")
    fun applyInsets(view: View) {
        val left = view.paddingLeft
        val top = view.paddingTop
        val right = view.paddingRight
        val bottom = view.paddingBottom
        view.setOnApplyWindowInsetsListener { target, insets ->
            target.setPadding(left + insets.systemWindowInsetLeft, top + insets.systemWindowInsetTop, right + insets.systemWindowInsetRight, bottom + insets.systemWindowInsetBottom)
            insets
        }
    }
}

/** Android 레이아웃 간격은 기기 밀도와 무관한 dp로 지정합니다. */
internal fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
