package kr.ac.lecture.mobilegame.foundation.graphics

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils

/** OpenGL 리소스의 생성과 해제를 한곳에서 관리합니다(Unity Resources/Unreal Asset Manager 대응). */
class ResourceManager(private val context: Context) {
    private val textures = mutableMapOf<Int, Texture>()

    fun texture(resourceId: Int): Texture = textures.getOrPut(resourceId) {
        val bitmap = requireNotNull(BitmapFactory.decodeResource(context.resources, resourceId))
        val handles = IntArray(1)
        GLES30.glGenTextures(1, handles, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, handles[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        val width = bitmap.width
        val height = bitmap.height
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        Texture(handles[0], width, height)
    }

    fun release() {
        if (textures.isNotEmpty()) GLES30.glDeleteTextures(textures.size, textures.values.map { it.handle }.toIntArray(), 0)
        textures.clear()
    }
}
