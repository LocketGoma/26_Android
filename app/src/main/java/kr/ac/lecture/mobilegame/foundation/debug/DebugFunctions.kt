package kr.ac.lecture.mobilegame.foundation.debug

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kr.ac.lecture.mobilegame.BuildConfig

private const val LOG_TAG = "MobileGameLecture"

/** Debug 출력에 필요한 Application Context만 보관합니다. */
internal object DebugRuntime {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    fun display(message: String) {
        val context = applicationContext ?: return
        mainHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * 문자열을 Logcat에 출력하고, 기본적으로 화면에도 Toast로 표시하는 top-level function입니다.
 * Kotlin에서는 `debugPrint("score=$score")` 같은 문자열 템플릿을 우선 사용하세요.
 * C/C++의 printf 형식이 익숙하다면
 * `debugPrint(String.format("score=%d, hp=%d", score, hp))`처럼 작성할 수 있습니다.
 * `display = false`이면 Logcat에만 출력합니다.
 */
fun debugPrint(message: String, display: Boolean = true) {
    if (!BuildConfig.DEBUG_BLOCK) return
    Log.d(LOG_TAG, message)
    if (display) DebugRuntime.display(message)
}

/** Int, Float, 배열, null 등 범용 값을 읽기 좋은 문자열로 바꾸는 top-level function입니다. */
fun debugString(value: Any?): String = when (value) {
    null -> "null"
    is Array<*> -> value.contentDeepToString()
    is BooleanArray -> value.contentToString()
    is ByteArray -> value.contentToString()
    is CharArray -> value.contentToString()
    is ShortArray -> value.contentToString()
    is IntArray -> value.contentToString()
    is LongArray -> value.contentToString()
    is FloatArray -> value.contentToString()
    is DoubleArray -> value.contentToString()
    else -> value.toString()
}
