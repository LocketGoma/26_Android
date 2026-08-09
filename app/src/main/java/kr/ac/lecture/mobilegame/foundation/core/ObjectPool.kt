package kr.ac.lecture.mobilegame.foundation.core

/** 이후 '오브젝트 제거 개선 → Object Pool' 실습에서 확장할 작은 기반입니다. */
class ObjectPool<T>(private val factory: () -> T) {
    private val available = ArrayDeque<T>()
    fun obtain(): T = available.removeLastOrNull() ?: factory()
    fun recycle(value: T) { available.addLast(value) }
    fun clear() = available.clear()
}
