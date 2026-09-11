package kr.ac.lecture.mobilegame.game

import android.content.Context

/** SharedPreferences에 상위 5개 점수만 저장하는 프로토타입 Leaderboard입니다. */
class LeaderboardStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun scores(): List<Int> = preferences.getString(SCORES_KEY, "")
        .orEmpty()
        .split(',')
        .mapNotNull(String::toIntOrNull)
        .sortedDescending()
        .take(MAX_SCORES)

    fun save(score: Int): List<Int> {
        val next = (scores() + score.coerceAtLeast(0)).sortedDescending().take(MAX_SCORES)
        preferences.edit().putString(SCORES_KEY, next.joinToString(",")).apply()
        return next
    }

    companion object {
        private const val PREFERENCES_NAME = "local_leaderboard"
        private const val SCORES_KEY = "scores"
        const val MAX_SCORES = 5
    }
}
