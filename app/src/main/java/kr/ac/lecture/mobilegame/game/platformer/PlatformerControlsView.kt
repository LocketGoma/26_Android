package kr.ac.lecture.mobilegame.game.platformer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kr.ac.lecture.mobilegame.R
import kr.ac.lecture.mobilegame.foundation.input.DigitalInput
import kr.ac.lecture.mobilegame.foundation.input.GameAction

/** 게임 화면 위에 세 버튼을 그립니다. 손가락마다 입력을 따로 기록해 이동 중 점프를 받습니다. */
class PlatformerControlsView(context: Context, private val controls: DigitalInput) : View(context) {
    private val regions = linkedMapOf(GameAction.LEFT to RectF(), GameAction.RIGHT to RectF(), GameAction.JUMP to RectF())
    private val fingers = mutableMapOf<Int, GameAction>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var safeLeft = 0
    private var safeRight = 0
    private var safeBottom = 0
    private var gameplayEnabled = true

    init { isClickable = true }

    fun setSafeInsets(left: Int, right: Int, bottom: Int) {
        safeLeft = left; safeRight = right; safeBottom = bottom
        placeButtons()
    }

    fun setGameplayEnabled(enabled: Boolean) {
        if (gameplayEnabled == enabled) return
        gameplayEnabled = enabled
        if (!enabled) clearInputs()
        invalidate()
    }

    fun clearInputs() { fingers.clear(); controls.clear(); invalidate() }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clearInputs()
        placeButtons()
    }

    private fun placeButtons() {
        val diameter = minOf(dp(60f), width / 5f, height / 3f)
        val bottom = height - safeBottom - dp(8f)
        val left = safeLeft + dp(16f)
        val right = width - safeRight - dp(16f)
        regions.getValue(GameAction.LEFT).set(left, bottom - diameter, left + diameter, bottom)
        regions.getValue(GameAction.RIGHT).set(left + diameter + dp(12f), bottom - diameter,
            left + diameter * 2f + dp(12f), bottom)
        regions.getValue(GameAction.JUMP).set(right - diameter, bottom - diameter, right, bottom)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((action, bounds) in regions) {
            val pressed = action in fingers.values
            paint.style = Paint.Style.FILL
            paint.color = if (pressed) Color.argb(175, 52, 94, 66) else Color.argb(if (gameplayEnabled) 100 else 45, 20, 35, 25)
            canvas.drawOval(bounds, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(2f)
            paint.color = Color.argb(if (gameplayEnabled) 200 else 85, 255, 255, 255)
            canvas.drawOval(bounds, paint)
            paint.style = Paint.Style.FILL
            paint.textSize = bounds.height() * 0.5f
            paint.textAlign = Paint.Align.CENTER
            val label = resources.getString(when (action) {
                GameAction.LEFT -> R.string.move_left
                GameAction.RIGHT -> R.string.move_right
                else -> R.string.jump
            })
            val baseline = bounds.centerY() - (paint.ascent() + paint.descent()) / 2f
            canvas.drawText(label, bounds.centerX(), baseline, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!gameplayEnabled) return true
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val action = regions.entries.firstOrNull { it.value.contains(event.getX(index), event.getY(index)) }?.key
                if (action != null) fingers[event.getPointerId(index)] = action
                updateHeldButtons()
            }
            MotionEvent.ACTION_MOVE -> {
                val iterator = fingers.iterator()
                while (iterator.hasNext()) {
                    val (id, action) = iterator.next()
                    val index = event.findPointerIndex(id)
                    if (index < 0 || !regions.getValue(action).contains(event.getX(index), event.getY(index))) iterator.remove()
                }
                updateHeldButtons()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                fingers.remove(event.getPointerId(event.actionIndex))
                updateHeldButtons()
            }
            MotionEvent.ACTION_CANCEL -> clearInputs()
        }
        return true
    }

    private fun updateHeldButtons() {
        for (action in regions.keys) controls.set(action, action in fingers.values)
        invalidate()
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density
}
