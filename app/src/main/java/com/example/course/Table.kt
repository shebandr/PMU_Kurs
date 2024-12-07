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
        // Tabletop top face
        -1f, 0.1f, 1f,   // 0
        -1f, 0.1f, -1f,  // 1
        1f, 0.1f, -1f,   // 2
        1f, 0.1f, 1f,    // 3
        // Tabletop bottom face
        -1f, -0.1f, 1f,  // 4
        -1f, -0.1f, -1f, // 5
        1f, -0.1f, -1f,  // 6
        1f, -0.1f, 1f,   // 7
        // Tabletop front face
        -1f, 0.1f, 1f,   // 8
        -1f, -0.1f, 1f,  // 9
        1f, -0.1f, 1f,   // 10
        1f, 0.1f, 1f,    // 11
        // Tabletop back face
        -1f, 0.1f, -1f,  // 12
        -1f, -0.1f, -1f, // 13
        1f, -0.1f, -1f,  // 14
        1f, 0.1f, -1f,   // 15
        // Tabletop left face
        -1f, 0.1f, 1f,   // 16
        -1f, -0.1f, 1f,  // 17
        -1f, -0.1f, -1f, // 18
        -1f, 0.1f, -1f,  // 19
        // Tabletop right face
        1f, 0.1f, 1f,    // 20
        1f, -0.1f, 1f,   // 21
        1f, -0.1f, -1f,  // 22
        1f, 0.1f, -1f,   // 23
        // Front left leg
        -0.8f, 0f, 0.8f, // 24
        -0.8f, -2f, 0.8f,// 25
        -0.7f, -2f, 0.8f,// 26
        -0.7f, 0f, 0.8f, // 27
        -0.8f, 0f, 0.7f, // 28
        -0.8f, -2f, 0.7f,// 29
        -0.7f, -2f, 0.7f,// 30
        -0.7f, 0f, 0.7f, // 31
        // Front right leg
        0.7f, 0f, 0.8f,  // 32
        0.7f, -2f, 0.8f, // 33
        0.8f, -2f, 0.8f, // 34
        0.8f, 0f, 0.8f,  // 35
        0.7f, 0f, 0.7f,  // 36
        0.7f, -2f, 0.7f, // 37
        0.8f, -2f, 0.7f, // 38
        0.8f, 0f, 0.7f,  // 39
        // Back left leg
        -0.8f, 0f, -0.7f,// 40
        -0.8f, -2f, -0.7f,//41
        -0.7f, -2f, -0.7f,//42
        -0.7f, 0f, -0.7f, //43
        -0.8f, 0f, -0.8f, //44
        -0.8f, -2f, -0.8f,//45
        -0.7f, -2f, -0.8f,//46
        -0.7f, 0f, -0.8f, //47
        // Back right leg
        0.7f, 0f, -0.8f,  //48
        0.7f, -2f, -0.8f, //49
        0.8f, -2f, -0.8f, //50
        0.8f, 0f, -0.8f,  //51
        0.7f, 0f, -0.7f,  //52
        0.7f, -2f, -0.7f, //53
        0.8f, -2f, -0.7f, //54
        0.8f, 0f, -0.7f   //55
    )

    private val normals = floatArrayOf(
        // Tabletop top face
        0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f,
        // Tabletop bottom face
        0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f,
        // Tabletop front face
        0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f,
        // Tabletop back face
        0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f,
        // Tabletop left face
        -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f,
        // Tabletop right face
        1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f,
        // Front left leg
        0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f,
        0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f,
        // Front right leg
        0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f,
        0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f,
        // Back left leg
        0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f,
        0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f,
        // Back right leg
        0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f,
        0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f
    )


    private val texCoords = floatArrayOf(
        // Tabletop top face
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        // Tabletop bottom face
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        // Tabletop front face
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        // Tabletop back face
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        // Tabletop left face
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        // Tabletop right face
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        // Front left leg
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        // Front right leg
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        // Back left leg
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        // Back right leg
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f,
        0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f
    )
    private val indices = shortArrayOf(
        // Tabletop top face
        0, 1, 2, 0, 2, 3,
        // Tabletop bottom face
        4, 5, 6, 4, 6, 7,
        // Tabletop front face
        8, 9, 10, 8, 10, 11,
        // Tabletop back face
        12, 13, 14, 12, 14, 15,
        // Tabletop left face
        16, 17, 18, 16, 18, 19,
        // Tabletop right face
        20, 21, 22, 20, 22, 23,
        // Front left leg
        24, 25, 26, 24, 26, 27,
        28, 29, 30, 28, 30, 31,
        // Front right leg
        32, 33, 34, 32, 34, 35,
        36, 37, 38, 36, 38, 39,
        // Back left leg
        40, 41, 42, 40, 42, 43,
        44, 45, 46, 44, 46, 47,
        // Back right leg
        48, 49, 50, 48, 50, 51,
        52, 53, 54, 52, 54, 55
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

    // Compute normal in eye space
    vNormal = normalize(mat3(uNormalMatrix) * aNormal); // Преобразуем нормаль с нормализацией

    // Compute light direction and view direction in eye space
    vec3 worldPos = vec3(uMVPMatrix * vPosition);
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
    float diff = max(dot(norm, normalize(vLightDir)), 0.0); // Нормализация светового направления
    vec3 reflectDir = reflect(-normalize(vLightDir), norm); // Инвертируем световое направление для отражения
    float spec = pow(max(dot(normalize(vViewDir), reflectDir), 0.0), 64.0); // Shininess factor

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