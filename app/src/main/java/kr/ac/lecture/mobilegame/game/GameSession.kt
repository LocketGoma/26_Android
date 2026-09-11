package kr.ac.lecture.mobilegame.game

enum class GameState { START, NORMAL, BOSS, GAME_OVER, LEADERBOARD }

/**
 * 한 판의 숫자와 상태만 보관하는 작은 모델입니다.
 * Android나 OpenGL에 의존하지 않아 Cycle, Life, Bomb 규칙을 단위 테스트할 수 있습니다.
 */
class GameSession {
    var state: GameState = GameState.START
        private set
    var cycle: Int = 1
        private set
    var life: Int = GameConfig.PLAYER_MAX_LIFE
        private set
    var bombs: Int = GameConfig.INITIAL_BOMB_COUNT
        private set
    var score: Int = 0
        private set
    var normalElapsed: Float = 0f
        private set
    var invincibleRemaining: Float = 0f
        private set

    private var leaderboardReturnState = GameState.START

    val isPlaying: Boolean get() = state == GameState.NORMAL || state == GameState.BOSS
    val isInvincible: Boolean get() = invincibleRemaining > 0f
    val timerSeconds: Int
        get() = (GameConfig.NORMAL_PHASE_SECONDS - normalElapsed).coerceAtLeast(0f)
            .toInt().coerceAtMost(99)

    fun startGame() {
        cycle = 1
        life = GameConfig.PLAYER_MAX_LIFE
        bombs = GameConfig.INITIAL_BOMB_COUNT
        score = 0
        normalElapsed = 0f
        invincibleRemaining = 0f
        state = GameState.NORMAL
    }

    /** @return Normal Phase가 이번 update에서 끝났으면 true */
    fun update(deltaTime: Float): Boolean {
        if (!isPlaying) return false
        invincibleRemaining = (invincibleRemaining - deltaTime).coerceAtLeast(0f)
        if (state != GameState.NORMAL) return false

        normalElapsed += deltaTime
        if (normalElapsed < GameConfig.NORMAL_PHASE_SECONDS) return false
        normalElapsed = GameConfig.NORMAL_PHASE_SECONDS
        state = GameState.BOSS
        return true
    }

    fun finishBoss() {
        if (state != GameState.BOSS) return
        cycle += 1
        normalElapsed = 0f
        state = GameState.NORMAL
    }

    /** @return 실제로 피해를 받아 Life가 줄었으면 true */
    fun takeHit(): Boolean {
        if (!isPlaying || isInvincible) return false
        life = (life - 1).coerceAtLeast(0)
        bombs = GameConfig.INITIAL_BOMB_COUNT
        invincibleRemaining = GameConfig.INVINCIBLE_SECONDS
        if (life == 0) state = GameState.GAME_OVER
        return true
    }

    fun collectLife() {
        if (isPlaying) life = (life + 1).coerceAtMost(GameConfig.PLAYER_MAX_LIFE)
    }

    fun collectBomb() {
        if (isPlaying) bombs = (bombs + 1).coerceAtMost(GameConfig.MAX_BOMB_COUNT)
    }

    fun useBomb(): Boolean {
        if (!isPlaying || bombs <= 0) return false
        bombs -= 1
        return true
    }

    fun addScore(points: Int) {
        if (points > 0) score += points
    }

    fun openLeaderboard() {
        if (state == GameState.START || state == GameState.GAME_OVER) {
            leaderboardReturnState = state
            state = GameState.LEADERBOARD
        }
    }

    fun closeLeaderboard() {
        if (state == GameState.LEADERBOARD) state = leaderboardReturnState
    }

    fun shouldDrawPlayer(): Boolean {
        if (!isInvincible) return true
        val elapsed = GameConfig.INVINCIBLE_SECONDS - invincibleRemaining
        return (elapsed / 0.12f).toInt() % 2 == 0
    }
}
