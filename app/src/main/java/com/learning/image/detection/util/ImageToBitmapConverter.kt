package com.learning.image.detection.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import androidx.camera.core.ImageProxy

object ImageToBitmapConverter {

    private lateinit var bitmapBuffer: Bitmap

    @SuppressLint("UnsafeOptInUsageError")
    fun toBitmap(context: Context, imageProxy: ImageProxy): Bitmap? {

        val yuvToRgbConverter = YuvToRgbConverter(context)

        val image = imageProxy.image ?: return null

        val rotationMatrix = Matrix()
        // Initialise Buffer
        if (!::bitmapBuffer.isInitialized) {

            rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            bitmapBuffer = Bitmap.createBitmap(
                imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
            )
        }

        // Pass image to an image analyser
        yuvToRgbConverter.yuvToRgb(image, bitmapBuffer)

        // Create the Bitmap in the correct orientation
        return Bitmap.createBitmap(
            bitmapBuffer,
            0,
            0,
            bitmapBuffer.width,
            bitmapBuffer.height,
            rotationMatrix,
            false
        )
    }
}