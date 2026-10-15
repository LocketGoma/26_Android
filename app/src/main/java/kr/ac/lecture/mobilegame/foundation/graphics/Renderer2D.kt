package kr.ac.lecture.mobilegame.foundation.graphics

import android.opengl.GLES30
import kr.ac.lecture.mobilegame.foundation.core.Vec2
import kr.ac.lecture.mobilegame.foundation.graphics.sprite.SpriteRegion
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/** 빨강·초록·파랑·불투명도를 0..1 값으로 전달합니다. alpha=0은 투명, 1은 불투명입니다. */
data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float = 1f) {
    companion object {
        val CYAN = Color(0.20f, 0.80f, 1.00f)
        val RED = Color(1.00f, 0.25f, 0.30f)
        val YELLOW = Color(1.00f, 0.85f, 0.20f)
        val WHITE = Color(1f, 1f, 1f)
    }
}

/** 사각형이나 그림을 OpenGL ES 3.0으로 출력합니다. 물체 이동과 게임 규칙은 계산하지 않습니다. */
class Renderer2D {
    private val viewport = Viewport2D()
    fun setViewport(width: Int, height: Int) { viewport.resize(width, height) }
    fun pixelSizeToWorld(width: Float, height: Float): Vec2 = viewport.pixelSizeToWorld(width, height)
    /** 세로형 화면에서도 Sprite가 원본 픽셀 종횡비를 유지하도록 월드 크기를 계산합니다. */
    fun aspectCorrectedSize(sprite: SpriteRegion, worldWidth: Float): Vec2 =
        viewport.aspectCorrectedSize(worldWidth, sprite.widthPixels / sprite.heightPixels)
    private var program = 0
    private var colorLocation = -1
    private var useTextureLocation = -1
    private var textureLocation = -1
    // 사각형의 정점 6개 × (위치 2 + UV 2). 같은 버퍼를 재사용합니다.
    private val vertices = ByteBuffer.allocateDirect(6 * 4 * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder()).asFloatBuffer()
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
        colorLocation = GLES30.glGetUniformLocation(program, "uColor")
        useTextureLocation = GLES30.glGetUniformLocation(program, "uUseTexture")
        textureLocation = GLES30.glGetUniformLocation(program, "uTexture")
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
        flipX: Boolean = false,
    ) {
        drawQuad(
            center, size,
            if (flipX) sprite.uRight else sprite.uLeft, sprite.vTop,
            if (flipX) sprite.uLeft else sprite.uRight, sprite.vBottom,
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
        vertices.position(0)
        putVertex(left, bottom, uLeft, vBottom, center, rotation)
        putVertex(right, bottom, uRight, vBottom, center, rotation)
        putVertex(right, top, uRight, vTop, center, rotation)
        putVertex(left, bottom, uLeft, vBottom, center, rotation)
        putVertex(right, top, uRight, vTop, center, rotation)
        putVertex(left, top, uLeft, vTop, center, rotation)

        GLES30.glUseProgram(program)
        // 호출자는 보통의 RGBA를 전달합니다. Bitmap과 같은 premultiplied alpha로 여기서 통일합니다.
        GLES30.glUniform4f(colorLocation, color.red * color.alpha, color.green * color.alpha,
            color.blue * color.alpha, color.alpha)
        val useTexture = textureHandle != 0
        GLES30.glUniform1i(useTextureLocation, if (useTexture) 1 else 0)
        if (useTexture) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)
            GLES30.glUniform1i(textureLocation, 0)
        }

        bindAttribute(vertices, index = 0, offsetFloats = 0)
        bindAttribute(vertices, index = 1, offsetFloats = 2)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    private fun putVertex(x: Float, y: Float, u: Float, v: Float, center: Vec2, rotation: Float) {
        if (rotation == 0f) {
            vertices.put(x).put(y)
        } else {
            val offset = viewport.rotateOffset(x - center.x, y - center.y, rotation)
            vertices.put(center.x + offset.x).put(center.y + offset.y)
        }
        vertices.put(u).put(v)
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
