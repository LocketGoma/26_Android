package kr.ac.lecture.mobilegame.game

/** 속도는 월드 단위/초, 시간은 초입니다. 이 값을 바꾸고 실행 결과를 비교해 보세요. */
object GameConfig {
    const val PLAYER_SPEED = 2.4f
    const val BULLET_SPEED = 1.8f
    const val FIRE_INTERVAL_SECONDS = 0.28f
    const val ENEMY_SCORE = 10
    const val INITIAL_ENEMY_SPAWN_INTERVAL_SECONDS = 1f
    const val MIN_ENEMY_SPAWN_INTERVAL_SECONDS = 0.35f
    const val INITIAL_ENEMY_SPEED = 0.42f

    /** 시간이 지날수록 생성 간격을 줄이되 최솟값보다 짧아지지 않게 합니다. */
    fun enemySpawnInterval(elapsedSeconds: Float): Float =
        (INITIAL_ENEMY_SPAWN_INTERVAL_SECONDS - elapsedSeconds * 0.01f)
            .coerceAtLeast(MIN_ENEMY_SPAWN_INTERVAL_SECONDS)

    fun enemySpeed(elapsedSeconds: Float): Float = INITIAL_ENEMY_SPEED + elapsedSeconds * 0.004f

    // TODO(데이터 실습): JSON 또는 Data Asset 성격의 외부 파일로 분리합니다.
}
