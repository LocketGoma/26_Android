package kr.ac.lecture.mobilegame.foundation.graphics

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils

/**
 * SpriteRegion이 공유하는 그림 정보입니다. GL 컨텍스트 재생성 시 handle만 교체합니다.
 * 외부에서는 읽기만 하고 ResourceManager가 GPU 수명을 관리합니다.
 */
class Texture(handle: Int, val width: Int, val height: Int) {
    var handle: Int = handle
        internal set
}

/** OpenGL Resource의 생성과 해제를 한곳에서 담당하는 Resource Manager입니다(Unity Resources/Unreal Asset Manager 대응). */
class ResourceManager(private val context: Context, private val nearestFiltering: Boolean = false) {
    private val textures = mutableMapOf<Int, Texture>()

    // Android Resource ID의 entry name은 파일 확장자를 포함하지 않습니다.
    fun resourceName(resourceId: Int): String = context.resources.getResourceEntryName(resourceId)

    fun texture(resourceId: Int): Texture = textures.getOrPut(resourceId) { upload(resourceId) }

    /** 새 컨텍스트의 GL 스레드에서 호출합니다. 이전 컨텍스트의 번호를 삭제하거나 재사용하지 않습니다. */
    fun reloadTextures() {
        for ((id, texture) in textures) {
            val next = upload(id)
            check(next.width == texture.width && next.height == texture.height)
            texture.handle = next.handle
        }
    }

    private fun upload(resourceId: Int): Texture {
        // Pixel Size 기준이 기기 밀도에 따라 달라지지 않도록 자동 밀도 확대를 끕니다.
        val bitmap = requireNotNull(BitmapFactory.decodeResource(context.resources, resourceId,
            BitmapFactory.Options().apply { inScaled = false }))
        val handles = IntArray(1)
        try {
            GLES30.glGenTextures(1, handles, 0)
            check(handles[0] != 0) { "Cannot allocate texture: $resourceId" }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, handles[0])
            val filter = if (nearestFiltering) GLES30.GL_NEAREST else GLES30.GL_LINEAR
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, filter)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, filter)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            return Texture(handles[0], bitmap.width, bitmap.height)
        } catch (error: RuntimeException) {
            if (handles[0] != 0) GLES30.glDeleteTextures(1, handles, 0)
            throw error
        } finally {
            bitmap.recycle()
        }
    }

    fun release() {
        if (textures.isNotEmpty()) GLES30.glDeleteTextures(textures.size, textures.values.map { it.handle }.toIntArray(), 0)
        textures.clear()
    }
}
