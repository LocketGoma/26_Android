package kr.ac.lecture.mobilegame

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** 앱 첫 화면에서는 게임만 고릅니다. 각 게임의 메뉴와 진행 상태는 해당 Activity가 관리합니다. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(32), dp(32), dp(32), dp(32))
            setBackgroundColor(Color.rgb(225, 237, 213))
        }
        root.addView(TextView(this).apply {
            setText(R.string.sample_menu_title)
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(41, 56, 46))
            setPadding(0, 0, 0, dp(32))
        })
        fun addEntry(label: Int, activity: Class<out Activity>) {
            root.addView(Button(this).apply {
                setText(label)
                textSize = 20f
                isAllCaps = false
                setOnClickListener { startActivity(Intent(this@MainActivity, activity)) }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, dp(12), 0, dp(12)) })
        }
        addEntry(R.string.shooter_sample, ShooterActivity::class.java)
        addEntry(R.string.platformer_sample, PlatformerActivity::class.java)
        SampleScreen.applyInsets(root)
        setContentView(root)
    }
}
