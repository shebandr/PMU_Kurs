package com.example.course;

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class WatermelonPulp {
    private val vertices = mutableListOf<Float>()
    private val texCoords = mutableListOf<Float>()
    private val normals = mutableListOf<Float>()
    private val indices = mutableListOf<Short>()
    private var vertexBuffer: FloatBuffer
    private var texCoordBuffer: FloatBuffer
    private var normalBuffer: FloatBuffer
    private var indexBuffer: ByteBuffer
    private val shaderProgram: ShaderProgram


    init {
        val sliceAngle = Math.PI / 2 // Угол 90 градусов
        val segments = 100
        val radius = 0.3f
        val thickness = 0.02f // Толщина дольки

        // Верхняя грань
        for (i in 0..segments) {
            val angle = -sliceAngle / 2 + i * (sliceAngle / segments)
            val x = (radius * cos(angle)).toFloat()
            val y = (radius * sin(angle)).toFloat()

            // Верхние точки
            vertices.add(x)
            vertices.add(y)
            vertices.add(thickness / 2)

            // Нормали для освещения
            normals.add(x)
            normals.add(y)
            normals.add(0f) // Нормаль будет вдоль оси Z для верхней грани

            // Текстурные координаты
            texCoords.add((x / radius + 1f) / 2f) // U
            texCoords.add((y / radius + 1f) / 2f) // V
        }

        // Нижняя грань
        for (i in 0..segments) {
            val angle = -sliceAngle / 2 + i * (sliceAngle / segments)
            val x = (radius * cos(angle)).toFloat()
            val y = (radius * sin(angle)).toFloat()

            // Нижние точки
            vertices.add(x)
            vertices.add(y)
            vertices.add(-thickness / 2)

            // Нормали для нижней грани
            normals.add(-x)
            normals.add(-y)
            normals.add(0f) // Нормаль будет вдоль оси Z для нижней грани

            // Текстурные координаты
            texCoords.add((x / radius + 1f) / 2f) // U
            texCoords.add((y / radius + 1f) / 2f) // V
        }

        // Добавляем центральную точку для нижней грани
        val centerBottomIndex = vertices.size / 3
        vertices.add(0f) // Центр нижней грани (X)
        vertices.add(0f) // Центр нижней грани (Y)
        vertices.add(-thickness / 2) // Z-координата нижней грани

        normals.add(0f)
        normals.add(0f)
        normals.add(-1f) // Нормаль для центра нижней грани

        texCoords.add(0.5f) // U-координата текстуры
        texCoords.add(0.5f) // V-координата текстуры

        // Индексы для верхней и нижней граней
        for (i in 0 until segments) {
            // Верхняя грань
            indices.add(0.toShort()) // Центр верхней грани
            indices.add((i + 1).toShort()) // Текущая вершина
            indices.add((i + 2).toShort()) // Следующая вершина

            // Нижняя грань
            indices.add(centerBottomIndex.toShort()) // Центр нижней грани
            indices.add((i + segments + 1 + 1).toShort()) // Текущая вершина нижней грани
            indices.add((i + segments + 1 + 2).toShort()) // Следующая вершина нижней грани
        }

        // Индексы для боковых поверхностей
        for (i in 0 until segments) {
            val top1 = i
            val top2 = (i + 1) % (segments + 1)
            val bottom1 = i + (segments + 1)
            val bottom2 = (i + 1) % (segments + 1) + (segments + 1)

            // Первый треугольник
            indices.add(top1.toShort())
            indices.add(bottom1.toShort())
            indices.add(top2.toShort())

            // Второй треугольник
            indices.add(top2.toShort())
            indices.add(bottom1.toShort())
            indices.add(bottom2.toShort())
        }

        // Создаем буферы
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices.toFloatArray()).position(0)

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        texCoordBuffer.put(texCoords.toFloatArray()).position(0)

        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        normalBuffer.put(normals.toFloatArray()).position(0)

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
        indices.forEach { indexBuffer.putShort(it) }
        indexBuffer.position(0)

        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uNormalMatrix;
            uniform vec3 uLightPos;
            uniform vec3 uViewPos;
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            attribute vec3 aNormal;
            varying vec2 vTexCoord;
            varying vec3 vNormal;
            varying vec3 vLightDir;
            varying vec3 vViewDir;
            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vNormal = normalize(vec3(uNormalMatrix * vec4(aNormal, 0.0)));
                vLightDir = normalize(uLightPos - vec3(gl_Position));
                vViewDir = normalize(uViewPos - vec3(gl_Position));
                vTexCoord = aTexCoord;
            }
        """
        val fragmentShaderCode = """
            precision mediump float;
            varying vec2 vTexCoord;
            varying vec3 vNormal;
            varying vec3 vLightDir;
            varying vec3 vViewDir;
            uniform sampler2D uTexture;
            void main() {
                vec4 texColor = texture2D(uTexture, vTexCoord);
                vec3 norm = normalize(vNormal);
                float diff = max(dot(norm, vLightDir), 0.0);
                vec3 reflectDir = reflect(-vLightDir, norm);
                float spec = pow(max(dot(vViewDir, reflectDir), 0.0), 32.0);
                vec3 ambient = vec3(1.0) * texColor.rgb; // Увеличьте амбиентное освещение
                float diffuse = max(dot(norm, vLightDir), 0.1);  // Повышаем минимальную интенсивность диффузного света
                vec3 specular = spec * vec3(1.0);
                vec3 finalColor = ambient + diffuse + specular;
                gl_FragColor = vec4(finalColor, texColor.a);
            }
        """

        shaderProgram = ShaderProgram(vertexShaderCode, fragmentShaderCode)
    }

    fun draw(mvpMatrix: FloatArray, normalMatrix: FloatArray, lightPos: FloatArray, viewPos: FloatArray, texture: Int) {
        // Используем шейдерную программу
        shaderProgram.use()

        // Получаем атрибуты
        val positionHandle = shaderProgram.getAttributeLocation("aPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        val texCoordHandle = shaderProgram.getAttributeLocation("aTexCoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)

        val normalHandle = shaderProgram.getAttributeLocation("aNormal")
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer)

        // Получаем униформы
        val mvpMatrixHandle = shaderProgram.getUniformLocation("uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        val normalMatrixHandle = shaderProgram.getUniformLocation("uNormalMatrix")
        GLES20.glUniformMatrix4fv(normalMatrixHandle, 1, false, normalMatrix, 0)

        val lightPosHandle = shaderProgram.getUniformLocation("uLightPos")
        GLES20.glUniform3fv(lightPosHandle, 1, lightPos, 0)

        val viewPosHandle = shaderProgram.getUniformLocation("uViewPos")
        GLES20.glUniform3fv(viewPosHandle, 1, viewPos, 0)

        val textureHandle = shaderProgram.getUniformLocation("uTexture")
        GLES20.glUniform1i(textureHandle, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)

        // Рисуем элементы
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        // Отключаем атрибуты
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
    }
}

