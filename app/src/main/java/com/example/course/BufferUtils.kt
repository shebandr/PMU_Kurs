package com.example.course

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object BufferUtils {
    fun createFloatBuffer(data: FloatArray): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(data.size * 4) // 4 байта на float
        buffer.order(ByteOrder.nativeOrder())
        return buffer.asFloatBuffer().apply {
            put(data)
            position(0)
        }
    }

    fun createShortBuffer(data: ShortArray): ShortBuffer {
        val buffer = ByteBuffer.allocateDirect(data.size * 2) // 2 байта на short
        buffer.order(ByteOrder.nativeOrder())
        return buffer.asShortBuffer().apply {
            put(data)
            position(0)
        }
    }
}
