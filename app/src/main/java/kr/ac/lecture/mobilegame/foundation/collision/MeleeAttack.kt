package kr.ac.lecture.mobilegame.foundation.collision

/** 선택 실습용 근접 판정입니다. 한 번의 공격에서는 같은 대상에게 한 번만 피해를 줍니다. */
class MeleeAttack(
    val range: Float = 1.1f,
    val duration: Float = 0.18f,
    val cooldown: Float = 0.40f,
) {
    init {
        require(range.isFinite() && range > 0f)
        require(duration.isFinite() && duration > 0f)
        require(cooldown.isFinite() && cooldown >= duration)
    }
    var activeRemaining = 0f
        private set
    var cooldownRemaining = 0f
        private set
    private val hitIds = mutableSetOf<Int>()
    val active get() = activeRemaining > 0f

    fun start(): Boolean {
        if (cooldownRemaining > 0f) return false
        activeRemaining = duration
        cooldownRemaining = cooldown
        hitIds.clear()
        return true
    }

    fun update(dt: Float) {
        require(dt.isFinite() && dt >= 0f)
        activeRemaining = (activeRemaining - dt).coerceAtLeast(0f)
        cooldownRemaining = (cooldownRemaining - dt).coerceAtLeast(0f)
    }

    fun bounds(x: Float, y: Float, bodyWidth: Float, bodyHeight: Float, facing: Int): Aabb =
        Aabb.fromCenter(x + (if (facing >= 0) 1 else -1) * (bodyWidth + range) / 2f,
            y, range, bodyHeight * 0.9f)

    fun tryHit(targetId: Int, attackBounds: Aabb, targetBounds: Aabb): Boolean =
        active && targetId !in hitIds && attackBounds.overlaps(targetBounds) && hitIds.add(targetId)

    fun reset() { activeRemaining = 0f; cooldownRemaining = 0f; hitIds.clear() }
}
