package com.learning.image.detection.presentation.analyzer

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetector
import com.learning.image.detection.presentation.view.BoundingBox
import com.learning.image.detection.data.DetectedObject
import com.learning.image.detection.ml.CerealModel
import com.learning.image.detection.util.ImageToBitmapConverter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model

private const val MAX_RESULT_DISPLAY = 3

class ImageAnalyzer(
    private val ctx: Context,
    private val objectDetector: ObjectDetector,
    private val layout: ViewGroup,
    private val listener: (detectedObject: List<DetectedObject>) -> Unit
) : ImageAnalysis.Analyzer {

    private val cerealModel: CerealModel by lazy {
        val compatList = CompatibilityList()
        val options = if (compatList.isDelegateSupportedOnThisDevice) {
            Model.Options.Builder().setDevice(Model.Device.GPU).build()
        } else {
            Model.Options.Builder().setNumThreads(4).build()
        }
        CerealModel.newInstance(ctx, options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        val items = mutableListOf<DetectedObject>()
        val bitmap = ImageToBitmapConverter.toBitmap(ctx, imageProxy)
        val tfImage = TensorImage.fromBitmap(bitmap)
        val outputs = cerealModel.process(tfImage)
            .probabilityAsCategoryList.apply {
                sortByDescending { it.score } // Sort with highest confidence first
            }.take(MAX_RESULT_DISPLAY) // take the top results

        for (output in outputs) {
            items.add(DetectedObject(output.label, output.score))
        }
        listener(items.toList())
        bitmap?.let {
            drawBoundingBox(imageProxy, it)
        }
        imageProxy.close()
    }


    /**
     * Draw bounding box from MLKit on preview!
     */
    private fun drawBoundingBox(imageProxy: ImageProxy, bitmap: Bitmap) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromBitmap(bitmap, rotationDegrees)
        objectDetector.process(image)
            .addOnSuccessListener { it ->
                it.map {
                    if (layout.childCount > 2) layout.removeViewAt(2)
                    val boundingBox = BoundingBox(ctx, it.boundingBox)
                    layout.addView(boundingBox, 2)
                }
            }
            .addOnFailureListener { // Task failed with an exception
            }
    }

}