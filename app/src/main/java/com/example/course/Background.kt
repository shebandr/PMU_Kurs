package com.example.course

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils

class Background(private val context: Context) {

    private val vertices = floatArrayOf(
        -1f, 1f, 0f,   // Верхний левый
        -1f, -1f, 0f,  // Нижний левый
        1f, -1f, 0f,   // Нижний правый
        1f, 1f, 0f     // Верхний правый
    )

    private val textureCoords = floatArrayOf(
        0f, 0f,  // Верхний левый
        0f, 1f,  // Нижний левый
        1f, 1f,  // Нижний правый
        1f, 0f   // Верхний правый
    )

    private val indices = shortArrayOf(0, 1, 2, 0, 2, 3)

    private val vertexBuffer = BufferUtils.createFloatBuffer(vertices)
    private val textureBuffer = BufferUtils.createFloatBuffer(textureCoords)
    private val indexBuffer = BufferUtils.createShortBuffer(indices)

    private var textureId: Int = 0

    init {
        //textureId = loadTexture(R.drawable.room2)
    }

    fun draw(mvpMatrix: FloatArray) {
        val program = ShaderUtils.createProgram(
            ShaderUtils.VERTEX_SHADER,
            ShaderUtils.FRAGMENT_SHADER
        )
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        val textureHandle = GLES20.glGetAttribLocation(program, "a_TexCoord")
        GLES20.glEnableVertexAttribArray(textureHandle)
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        val textureUniformHandle = GLES20.glGetUniformLocation(program, "u_Texture")
        GLES20.glUniform1i(textureUniformHandle, 0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indices.size,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureHandle)
    }

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

object ShaderUtils {
    const val VERTEX_SHADER = """
        attribute vec4 a_Position;
        attribute vec2 a_TexCoord;
        varying vec2 v_TexCoord;
        uniform mat4 u_MVPMatrix;

        void main() {
            gl_Position = u_MVPMatrix * a_Position;
            v_TexCoord = a_TexCoord;
        }
    """

    const val FRAGMENT_SHADER = """
        precision mediump float;
        uniform sampler2D u_Texture;
        varying vec2 v_TexCoord;

        void main() {
            gl_FragColor = texture2D(u_Texture, v_TexCoord);
        }
    """

    fun createProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(program)
            throw RuntimeException("Error creating program: ${GLES20.glGetProgramInfoLog(program)}")
        }

        return program
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Error compiling shader: ${GLES20.glGetShaderInfoLog(shader)}")
        }

        return shader
    }
}
