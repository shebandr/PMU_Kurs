package com.example.course

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Table(private val context: Context) {

    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val normalBuffer: FloatBuffer
    private var program: Int
    private var textureId: Int = 0
    private val vertices = floatArrayOf(
        // Столешница
        -1f, 0.1f, 1f,   // 0: передняя левая верх
        -1f, 0.1f, -1f,  // 1: задняя левая верх
        1f, 0.1f, -1f,   // 2: задняя правая верх
        1f, 0.1f, 1f,    // 3: передняя правая верх
        -1f, -0.1f, 1f,     // 4: передняя левая низ
        -1f, -0.1f, -1f,    // 5: задняя левая низ
        1f, -0.1f, -1f,     // 6: задняя правая низ
        1f, -0.1f, 1f,     // 7: передняя правая низ

        -0.8f, 0f, 0.8f,
        -0.8f, 0f, 0.7f,
        -0.7f, 0f, 0.8f,
        -0.7f, 0f, 0.7f,
        -0.8f, -2f, 0.8f,
        -0.8f, -2f, 0.7f,
        -0.7f, -2f, 0.8f,
        -0.7f, -2f, 0.7f,

        0.7f, 0f, 0.8f,
        0.7f, 0f, 0.7f,
        0.8f, 0f, 0.8f,
        0.8f, 0f, 0.7f,
        0.7f, -2f, 0.8f,
        0.7f, -2f, 0.7f,
        0.8f, -2f, 0.8f,
        0.8f, -2f, 0.7f,

        -0.8f, 0f, -0.7f,
        -0.8f, 0f, -0.8f,
        -0.7f, 0f, -0.7f,
        -0.7f, 0f, -0.8f,
        -0.8f, -2f, -0.7f,
        -0.8f, -2f, -0.8f,
        -0.7f, -2f, -0.7f,
        -0.7f, -2f, -0.8f,

        0.7f, 0f, -0.7f,
        0.7f, 0f, -0.8f,
        0.8f, 0f, -0.7f,
        0.8f, 0f, -0.8f,
        0.7f, -2f, -0.7f,
        0.7f, -2f, -0.8f,
        0.8f, -2f, -0.7f,
        0.8f, -2f, -0.8f
    )

    private val normals = floatArrayOf(
        // Нормали для каждой вершины
        0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f,
        0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f
    )

    private val texCoords = floatArrayOf(
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,

        // Текстурные координаты для передней левой ножки
        0f, 0f, 0f, 1f, 0.5f, 0f, 0.5f, 1f,

        // Текстурные координаты для передней правой ножки
        0f, 0f, 0f, 1f, 0.5f, 0f, 0.5f, 1f,

        // Текстурные координаты для задней левой ножки
        0f, 0f, 0f, 1f, 0.5f, 0f, 0.5f, 1f,

        // Текстурные координаты для задней правой ножки
        0f, 0f, 0f, 1f, 0.5f, 0f, 0.5f, 1f
    )

    private val indices = shortArrayOf(
        0, 1, 2, 0, 2, 3, // Верх столешницы
        4, 5, 6, 4, 6, 7, // Низ столешницы
        0, 1, 5, 0, 5, 4, // Левая грань
        2, 3, 7, 2, 7, 6, // Правая грань
        0, 3, 7, 0, 7, 4, // Передняя грань
        1, 2, 6, 1, 6, 5, // Задняя грань

        // Передняя левая ножка
        8, 9, 13, 8, 13, 12, // Задняя грань
        8, 10, 14, 8, 14, 12, // Боковая грань
        10, 11, 15, 10, 15, 14, // Передняя грань
        9, 11, 15, 9, 15, 13, // Боковая грань
        8, 9, 11, 8, 11, 10, // Верхняя крышка
        12, 13, 15, 12, 15, 14, // Нижняя крышка

        // Передняя правая ножка
        16, 17, 21, 16, 21, 20, // Задняя грань
        16, 18, 22, 16, 22, 20, // Боковая грань
        18, 19, 23, 18, 23, 22, // Передняя грань
        17, 19, 23, 17, 23, 21, // Боковая грань
        16, 17, 19, 16, 19, 18, // Верхняя крышка
        20, 21, 23, 20, 23, 22, // Нижняя крышка

        // Задняя левая ножка
        24, 25, 29, 24, 29, 28, // Задняя грань
        24, 26, 30, 24, 30, 28, // Боковая грань
        26, 27, 31, 26, 31, 30, // Передняя грань
        25, 27, 31, 25, 31, 29, // Боковая грань
        24, 25, 27, 24, 27, 26, // Верхняя крышка
        28, 29, 31, 28, 31, 30, // Нижняя крышка

        // Задняя правая ножка
        32, 33, 37, 32, 37, 36, // Задняя грань
        32, 34, 38, 32, 38, 36, // Боковая грань
        34, 35, 39, 34, 39, 38, // Передняя грань
        33, 35, 39, 33, 39, 37, // Боковая грань
        32, 33, 35, 32, 35, 34, // Верхняя крышка
        36, 37, 39, 36, 39, 38  // Нижняя крышка
    )

    init {
        // Создаем буферы
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(vertices)
                position(0)
            }

        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(normals)
                position(0)
            }

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(texCoords)
                position(0)
            }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder()).asShortBuffer().apply {
                put(indices)
                position(0)
            }

        // Компиляция и линковка шейдеров
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        program = GLES20.glCreateProgram().apply {
            GLES20.glAttachShader(this, vertexShader)
            GLES20.glAttachShader(this, fragmentShader)
            GLES20.glLinkProgram(this)
        }

        // Загрузка текстуры
        textureId = loadTexture(context, R.drawable.table)
    }

    private fun loadTexture(context: Context, resourceId: Int): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        return textureIds[0]
    }

    fun draw(mVPMatrix: FloatArray, normalMatrix: FloatArray, lightPos: FloatArray, viewPos: FloatArray) {
        GLES20.glUseProgram(program)

        // Привязка атрибутов и униформов
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)

        val normalHandle = GLES20.glGetAttribLocation(program, "aNormal")
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 12, normalBuffer)

        val matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mVPMatrix, 0)

        val normalMatrixHandle = GLES20.glGetUniformLocation(program, "uNormalMatrix")
        GLES20.glUniformMatrix4fv(normalMatrixHandle, 1, false, normalMatrix, 0)

        val lightPosHandle = GLES20.glGetUniformLocation(program, "uLightPos")
        GLES20.glUniform3fv(lightPosHandle, 1, lightPos, 0)

        val viewPosHandle = GLES20.glGetUniformLocation(program, "uViewPos")
        GLES20.glUniform3fv(viewPosHandle, 1, viewPos, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    companion object {
        private const val VERTEX_SHADER_CODE = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uNormalMatrix;
            uniform vec3 uLightPos;
            uniform vec3 uViewPos;
            attribute vec4 vPosition;
            attribute vec2 aTexCoord;
            attribute vec3 aNormal;
            varying vec2 vTexCoord;
            varying vec3 vNormal;
            varying vec3 vLightDir;
            varying vec3 vViewDir;

            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vTexCoord = aTexCoord;
                vNormal = normalize(vec3(uNormalMatrix * vec4(aNormal, 0.0)));
                vec3 worldPos = vec3(gl_Position);
                vLightDir = normalize(uLightPos - worldPos);
                vViewDir = normalize(uViewPos - worldPos);
            }
        """

        private const val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            varying vec2 vTexCoord;
            varying vec3 vNormal;
            varying vec3 vLightDir;
            varying vec3 vViewDir;
            uniform sampler2D uTexture;

            void main() {
                vec4 texColor = texture2D(uTexture, vTexCoord);
                
                // Normalize the normal vector
                vec3 norm = normalize(vNormal);
                
                // Compute the diffuse and specular lighting
                float diff = max(dot(norm, vLightDir), 0.0);
                vec3 reflectDir = reflect(-vLightDir, norm);
                float spec = pow(max(dot(vViewDir, reflectDir), 0.0), 32.0); // Shininess factor

                // Combine the color and lighting
                vec3 ambient = vec3(0.1) * texColor.rgb; // Ambient light
                vec3 diffuse = diff * texColor.rgb; // Diffuse light
                vec3 specular = spec * vec3(1.0); // Specular light color (white)

                vec3 finalColor = ambient + diffuse + specular;
                gl_FragColor = vec4(finalColor, texColor.a);
            }
        """
    }
}