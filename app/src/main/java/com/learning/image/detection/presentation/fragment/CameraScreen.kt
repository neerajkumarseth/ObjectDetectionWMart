package com.learning.image.detection.presentation.fragment

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.learning.image.detection.databinding.FragmentCameraBinding
import com.learning.image.detection.presentation.analyzer.ImageAnalyzer
import com.learning.image.detection.presentation.viewmodel.DetectedObjectListViewModel
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraScreen : Fragment() {

    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var preview: Preview
    private lateinit var camera: Camera
    private lateinit var previewView: PreviewView
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var fragmentCameraBinding: FragmentCameraBinding
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val viewModel: DetectedObjectListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previewView = fragmentCameraBinding.previewView
        previewView.post {
            initCamera()
        }
        viewModel.detectedObjectList.observe(viewLifecycleOwner) { it ->
            val prediction = it.maxByOrNull { it.score }
            fragmentCameraBinding.objectName.text =
                "${(prediction?.probabilityString)} ${prediction?.label}"
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder()
                    .build()
                val point = Point()
                val localModel = LocalModel.Builder()
                    .setAssetFilePath("cereal_model.tflite")
                    .build()

                val options = CustomObjectDetectorOptions.Builder(localModel)
                    .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                    .enableClassification()
                    .setClassificationConfidenceThreshold(0.5f)
                    .setMaxPerObjectLabelCount(3)
                    .build()

                val objectDetector = ObjectDetection.getClient(options)

                /* Cereal tflite has following properties : `
                   * type: float32[1,224,224,3] denotation: Image(RGB)
                   * Input image to be classified. The expected image is 224 x 224, with three channels
                   * (red, blue, and green) per pixel. Each value in the tensor is a single byte between 0 and 1.
                   */

                imageAnalyzer = ImageAnalysis.Builder()
                    .setTargetResolution(Size(point.x, point.y))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysisUseCase: ImageAnalysis ->
                        analysisUseCase.setAnalyzer(
                            cameraExecutor,
                            ImageAnalyzer(
                                requireContext(),
                                objectDetector,
                                fragmentCameraBinding.root
                            ) { items ->
                                // updating the list of recognised objects
                                viewModel.updateData(items)
                            })
                    }

                // Select camera, back is the default. If it is not available, choose front camera
                val cameraSelector =
                    if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA))
                        CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera - try to bind everything at once and CameraX will find
                    // the best combination.
                    camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalyzer
                    )

                    // Attach the preview to preview view, aka View Finder
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                } catch (exc: Exception) {
                }

            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    override fun onDestroyView() {
        // Terminate all outstanding analyzing jobs (if there is any).
        cameraExecutor.apply {
            shutdown()
            awaitTermination(1000, TimeUnit.MILLISECONDS)
        }
        super.onDestroyView()

    }
}
