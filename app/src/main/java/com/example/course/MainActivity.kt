package com.example.course

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenGLView(LocalContext.current)
        }
    }
    @Composable
    fun OpenGLView(context: Context) {
        AndroidView(factory = {
            GLSurfaceView(context).apply {
                setEGLContextClientVersion(2)
                setRenderer(Renderer(context))
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }
        })
    }
}