package com.example.course

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Renderer(private val context: Context) : GLSurfaceView.Renderer {

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mVPMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(16)
    private val lightPos = floatArrayOf(0.00f, 0.3f, 0f)
    private val cucumberInGlass = floatArrayOf(0.5f, 0.0f, -0.1f)

    private lateinit var table: Table
    private lateinit var glass: Glass
    private lateinit var candle: Candle
    private lateinit var candleFire: CandleFire
    private lateinit var apple: Sphere
    private lateinit var watermelon: Sphere
    private lateinit var orange: Sphere
    private lateinit var cucumber: Sphere
    private lateinit var watermelonPulp: WatermelonPulp
    private lateinit var background: Background




    private var appleTexture: Int = 0
    private var watermelonTexture: Int = 0
    private var orangeTexture: Int = 0
    private var cucumberTexture: Int = 0
    private var candleFireTexture: Int = 0
    private var watermelonPulpTexture: Int = 0
    override fun onSurfaceCreated(arg0: GL10?, arg1: EGLConfig?) {
        GLES20.glClearColor(0f,0f,0f, 0.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LESS)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        background = Background(context)

        table = Table(context)

        apple = Sphere(radius = 0.3f/3)
        appleTexture = loadTexture(R.drawable.apple)

        glass = Glass(context)

        watermelonPulp = WatermelonPulp()

        candle = Candle(context)
        candleFire = CandleFire(context, radius = 0.035f)
        candleFire.initialize()

        watermelon = Sphere(radius = 0.8f/3)
        watermelonTexture = loadTexture(R.drawable.watermelon)
        watermelonPulpTexture = loadTexture(R.drawable.arbux)

        orange = Sphere(radius = 0.35f/3)
        orangeTexture = loadTexture(R.drawable.orange)

        cucumber = Sphere(radius = 0.05f, stretchFactorY = 7f)
        cucumberTexture = loadTexture(R.drawable.cucumber)

    }

    override fun onDrawFrame(arg0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        Matrix.setLookAtM(viewMatrix, 0, 1f, 1f, 2f, 0f, 0f, 0f, 0f, 1f, 0f)
//Matrix.setLookAtM(viewMatrix, 0, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f, 0f)

// Установление единичной матрицы (дефолт позиция, масштаб, поворот)
        Matrix.setIdentityM(modelMatrix, 0)

// Преобразование из 3D в 2D
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

// Инверсия, чтобы правильно вычислить нормали
        Matrix.invertM(normalMatrix, 0, viewMatrix, 0)

// Перемещение вдоль осей
        Matrix.translateM(modelMatrix, 0, 0f, -0.3f, 0f)

// Поворот
        Matrix.rotateM(modelMatrix, 0, 0f, 0.1f, 0.1f, 0f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

// Итоговая матрица для рендеринга
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        Matrix.setIdentityM(modelMatrix, 0)
        table.draw(mVPMatrix, normalMatrix, lightPos, viewMatrix)

// Арбуз
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, -0.0f, 0.1f, -0.5f)
        Matrix.rotateM(modelMatrix, 0, 180f, 0f, 1f, 0f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        watermelon.draw(mVPMatrix, normalMatrix, lightPos, viewMatrix, watermelonTexture)

// Яблоко
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.3f, 0.0f, 0.70f)
        Matrix.rotateM(modelMatrix, 0, 180f, 0f, 1f, 0f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        apple.draw(mVPMatrix, normalMatrix, lightPos, viewMatrix, appleTexture)

// Апельсин
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.1f, 0.0f,  0.80f)
        Matrix.rotateM(modelMatrix, 0, 180f, 0f, 1f, 0f)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        orange.draw(mVPMatrix, normalMatrix, lightPos, viewMatrix, orangeTexture)

        // Огурец внутри стакана
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cucumberInGlass[0], cucumberInGlass[1]+0.2f, cucumberInGlass[2])
        Matrix.rotateM(modelMatrix, 0, 180f, 0f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, -50f, 0f, 1f, 0.15f) // Поворот вокруг оси Y
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        cucumber.draw(mVPMatrix, normalMatrix, lightPos, viewMatrix, cucumberTexture)

// Стакан
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cucumberInGlass[0], cucumberInGlass[1], cucumberInGlass[2])
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        glass.draw(mVPMatrix, lightPos, viewMatrix)



// Свеча
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.translateM(modelMatrix, 0, lightPos[0], lightPos[1]-0.3f, lightPos[2])
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        candle.draw(mVPMatrix, lightPos, viewMatrix)

// Пламя свечи
        val time = System.currentTimeMillis() % 10000L / 1000.0f // Время в секундах
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, lightPos[0], lightPos[1], lightPos[2])
        Matrix.multiplyMM(mVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0)
        candleFire.draw(mVPMatrix, time)
    }

    override fun onSurfaceChanged(arg0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 20f) // увеличен диапазон дальности

        Matrix.setLookAtM(viewMatrix, 0, 1f, 1f, 2f, 0f, 0f, 0f, 0f, 1f, 0f)

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

    private fun getTextureAspectRatio(resourceId: Int): Float {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(context.resources, resourceId, options)
        return options.outWidth.toFloat() / options.outHeight.toFloat()
    }

}