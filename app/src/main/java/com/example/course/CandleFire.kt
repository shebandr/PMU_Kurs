package com.example.course

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
class CandleFire(private val context: Context, private val radius: Float) {
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;

        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vTexCoord = aTexCoord; // Просто передаем текстурные координаты
        }
    """.trimIndent()

    private val fragmentShaderCode = """
    precision mediump float;
    uniform sampler2D uTexture;
    uniform float uTime;
    varying vec2 vTexCoord;

    void main() {
        // Добавляем волны по оси X и Y для текстуры
        float waveX = 0.05 * sin(uTime * 2.0 + vTexCoord.y * 10.0);  // Волна по X
        float waveY = 0.05 * cos(uTime * 3.0 + vTexCoord.x * 5.0);   // Волна по Y

        // Применяем смещения к текстурным координатам
        vec2 movedTexCoord = vTexCoord + vec2(waveX, waveY);

        // Получаем цвет из текстуры
        vec4 color = texture2D(uTexture, movedTexCoord);

        // Влияние времени на колебания пламени (интенсивность огня)
        float intensity = 0.6 + 0.4 * sin(uTime * 4.0 + vTexCoord.y * 15.0);

        gl_FragColor = vec4(color.rgb, color.a * intensity);
    }
""".trimIndent()

    private val vertices = floatArrayOf(
        -radius / 2,  radius * 5, 0f,   0f, 1f,   // Левый верхний угол
        0f,            radius * 5f, 0f,   0.3f, 1f,   // Центр (для закругления)
        radius / 2,     radius * 5, 0f,   1f, 1f,   // Правый верхний угол
        -radius,        -radius * 5, 0f,            0f, 0f,   // Левый нижний угол (увеличено вниз)
        radius,         -radius * 5, 0f,             1f, 0f    // Правый нижний угол (увеличено вниз)
    )

    private val indices = shortArrayOf(
        0, 1, 2, // Верхняя часть
        3, 1, 4  // Нижняя часть
    )

    private var program: Int = 0
    private var vertexBuffer = createFloatBuffer(vertices)
    private var indexBuffer = createShortBuffer(indices)
    private var textureId: Int = 0

    fun initialize() {
        program = createProgram(vertexShaderCode, fragmentShaderCode)
        textureId = loadTexture(R.drawable.fire) // Добавьте текстуру пламени
    }

    fun draw(mvpMatrix: FloatArray, time: Float) {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        val timeHandle = GLES20.glGetUniformLocation(program, "uTime")

        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 5 * 4, vertexBuffer)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(timeHandle, time)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
    }

    private fun createProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).also {
            GLES20.glShaderSource(it, vertexShaderCode)
            GLES20.glCompileShader(it)
        }

        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).also {
            GLES20.glShaderSource(it, fragmentShaderCode)
            GLES20.glCompileShader(it)
        }

        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    private fun createFloatBuffer(data: FloatArray) =
        ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(data).position(0) }

    private fun createShortBuffer(data: ShortArray) =
        ByteBuffer.allocateDirect(data.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply { put(data).position(0) }

    private fun loadTexture(resourceId: Int): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        val textureId = textureIds[0]

        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        bitmap.recycle()

        return textureId
    }
}
