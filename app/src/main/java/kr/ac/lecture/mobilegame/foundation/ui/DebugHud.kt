package kr.ac.lecture.mobilegame.foundation.ui

import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.core.GameObject
import kr.ac.lecture.mobilegame.foundation.graphics.Color
import kr.ac.lecture.mobilegame.foundation.graphics.Renderer2D

/** 이미지와 충돌 크기를 비교하도록 실제 CollisionComponent의 경계를 그립니다. */
class DebugHud : Hud<Iterable<GameObject>> {
    override fun draw(renderer: Renderer2D, state: Iterable<GameObject>) {
        for (obj in state) {
            if (!obj.active || obj.collision == null) continue
            val bounds = obj.bounds
            val width = bounds.right - bounds.left
            val height = bounds.maxY - bounds.minY
            val x = (bounds.left + bounds.right) / 2f
            val y = (bounds.minY + bounds.maxY) / 2f
            renderer.drawRect(Vec2(x, bounds.minY), Vec2(width, 0.006f), Color.CYAN)
            renderer.drawRect(Vec2(x, bounds.maxY), Vec2(width, 0.006f), Color.CYAN)
            renderer.drawRect(Vec2(bounds.left, y), Vec2(0.006f, height), Color.CYAN)
            renderer.drawRect(Vec2(bounds.right, y), Vec2(0.006f, height), Color.CYAN)
        }
    }
}
