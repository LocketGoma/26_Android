package kr.ac.lecture.mobilegame.foundation.graphics

import android.opengl.GLES30
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Renderer2D에 전달할 RGBA 값을 담는 immutable data class입니다.
 * Render 명령과 항상 함께 읽는 작은 데이터이므로 Renderer2D와 같은 파일에 둡니다.
 */
data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float = 1f) {
    companion object {
        val CYAN = Color(0.20f, 0.80f, 1.00f)
        val RED = Color(1.00f, 0.25f, 0.30f)
        val YELLOW = Color(1.00f, 0.85f, 0.20f)
        val WHITE = Color(1f, 1f, 1f)
    }
}

/** 색 사각형과 Texture UV 영역을 그리는 최소 OpenGL ES 3.0 Renderer입니다. */
class Renderer2D {
    private val viewport = Viewport2D()
    fun setViewport(width: Int, height: Int) { viewport.resize(width, height) }
    fun pixelSizeToWorld(width: Float, height: Float): Vec2 = viewport.pixelSizeToWorld(width, height)
    /** 세로형 화면에서도 Sprite가 원본 픽셀 종횡비를 유지하도록 월드 크기를 계산합니다. */
    fun aspectCorrectedSize(sprite: SpriteRegion, worldWidth: Float): Vec2 =
        viewport.aspectCorrectedSize(worldWidth, sprite.widthPixels / sprite.heightPixels)
    private var program = 0
    private val vertexShader = """
        #version 300 es
        layout(location = 0) in vec2 aPosition;
        layout(location = 1) in vec2 aUv;
        out vec2 vUv;
        void main() {
            gl_Position = vec4(aPosition, 0.0, 1.0);
            vUv = aUv;
        }
    """.trimIndent()
    private val fragmentShader = """
        #version 300 es
        precision mediump float;
        in vec2 vUv;
        uniform vec4 uColor;
        uniform bool uUseTexture;
        uniform sampler2D uTexture;
        out vec4 fragColor;
        void main() {
            fragColor = uUseTexture ? texture(uTexture, vUv) * uColor : uColor;
        }
    """.trimIndent()

    fun create() {
        val vertex = compile(GLES30.GL_VERTEX_SHADER, vertexShader)
        val fragment = compile(GLES30.GL_FRAGMENT_SHADER, fragmentShader)
        program = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertex)
            GLES30.glAttachShader(it, fragment)
            GLES30.glLinkProgram(it)
        }
        val status = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, status, 0)
        check(status[0] == GLES30.GL_TRUE) { "Program link failed: ${GLES30.glGetProgramInfoLog(program)}" }
        GLES30.glDeleteShader(vertex)
        GLES30.glDeleteShader(fragment)
        GLES30.glEnable(GLES30.GL_BLEND)
        // Android Bitmap은 premultiplied alpha이므로 GL_ONE을 사용합니다.
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun beginFrame() {
        GLES30.glClearColor(0.025f, 0.035f, 0.08f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
    }

    fun drawRect(center: Vec2, size: Vec2, color: Color, rotation: Float = 0f) {
        drawQuad(center, size, 0f, 0f, 1f, 1f, color, textureHandle = 0, rotation = rotation)
    }

    fun drawSprite(
        sprite: SpriteRegion,
        center: Vec2,
        size: Vec2,
        tint: Color = Color.WHITE,
        rotation: Float = 0f,
    ) {
        drawQuad(
            center, size,
            sprite.uLeft, sprite.vTop, sprite.uRight, sprite.vBottom,
            tint, sprite.texture.handle, rotation,
        )
    }

    private fun drawQuad(
        center: Vec2,
        size: Vec2,
        uLeft: Float,
        vTop: Float,
        uRight: Float,
        vBottom: Float,
        color: Color,
        textureHandle: Int,
        rotation: Float,
    ) {
        val left = center.x - size.x / 2f
        val right = center.x + size.x / 2f
        val top = center.y + size.y / 2f
        val bottom = center.y - size.y / 2f
        val vertices = floatArrayOf(
            left, bottom, uLeft, vBottom,
            right, bottom, uRight, vBottom,
            right, top, uRight, vTop,
            left, bottom, uLeft, vBottom,
            right, top, uRight, vTop,
            left, top, uLeft, vTop,
        )
        // 화면 비율을 반영한 Pixel 공간에서 회전한 다음 NDC로 되돌립니다.
        for (i in vertices.indices step 4) {
            val rotated = viewport.rotateOffset(vertices[i] - center.x, vertices[i + 1] - center.y, rotation)
            vertices[i] = center.x + rotated.x
            vertices[i + 1] = center.y + rotated.y
        }
        val buffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(vertices); position(0) }

        GLES30.glUseProgram(program)
        GLES30.glUniform4f(GLES30.glGetUniformLocation(program, "uColor"), color.red, color.green, color.blue, color.alpha)
        val useTexture = textureHandle != 0
        GLES30.glUniform1i(GLES30.glGetUniformLocation(program, "uUseTexture"), if (useTexture) 1 else 0)
        if (useTexture) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)
            GLES30.glUniform1i(GLES30.glGetUniformLocation(program, "uTexture"), 0)
        }

        bindAttribute(buffer, index = 0, offsetFloats = 0)
        bindAttribute(buffer, index = 1, offsetFloats = 2)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    private fun bindAttribute(buffer: FloatBuffer, index: Int, offsetFloats: Int) {
        buffer.position(offsetFloats)
        GLES30.glEnableVertexAttribArray(index)
        GLES30.glVertexAttribPointer(index, 2, GLES30.GL_FLOAT, false, 4 * Float.SIZE_BYTES, buffer)
    }

    fun release() {
        if (program != 0) GLES30.glDeleteProgram(program)
        program = 0
    }

    private fun compile(type: Int, source: String): Int = GLES30.glCreateShader(type).also { shader ->
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        val status = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
        check(status[0] == GLES30.GL_TRUE) { "Shader compile failed: ${GLES30.glGetShaderInfoLog(shader)}" }
    }
}

/** Pixel→NDC 변환을 Renderer 쪽에 모읍니다. 양의 Rotation은 화면에서 반시계 방향입니다. */
class Viewport2D {
    private var width = 1
    private var height = 1
    fun resize(width: Int, height: Int) {
        require(width > 0 && height > 0)
        this.width = width
        this.height = height
    }
    fun pixelSizeToWorld(width: Float, height: Float) = Vec2(width * 2f / this.width, height * 2f / this.height)
    fun aspectCorrectedSize(worldWidth: Float, widthToHeight: Float): Vec2 {
        require(worldWidth >= 0f && widthToHeight > 0f)
        val pixelWidth = worldWidth * width / 2f
        val pixelHeight = pixelWidth / widthToHeight
        return Vec2(worldWidth, pixelHeight * 2f / height)
    }
    fun rotateOffset(x: Float, y: Float, degrees: Float): Vec2 {
        val radians = Math.toRadians(degrees.toDouble())
        val cos = kotlin.math.cos(radians).toFloat()
        val sin = kotlin.math.sin(radians).toFloat()
        return Vec2(x * cos - y * height / width * sin, x * width / height * sin + y * cos)
    }
}
