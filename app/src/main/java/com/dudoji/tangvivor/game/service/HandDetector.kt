package com.dudoji.tangvivor.game.service

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

@Deprecated("No Use")
class HandDetector(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {

    companion object {
        private const val TAG = "HandDetector"
    }

    // CameraX
    private val controller = LifecycleCameraController(context).apply {
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    }

    val detectorOptions = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()
    val detectorClient = PoseDetection.getClient(detectorOptions)

    // Start
    fun start() {
        // Connect Controller to previewView
        previewView.controller = controller
        controller.bindToLifecycle(lifecycleOwner)


        // Analyzer
        val analyzer = ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                detectorClient.process(image)
                    .addOnSuccessListener { pose -> onPoseDetected(pose) }
                    .addOnFailureListener { e -> Log.e(TAG, "포즈 잡기 실패!") }
                    .addOnCompleteListener { imageProxy.close() }
            } else {
                imageProxy.close()
            }
        }

        // Give Analyzer to controller
        controller.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
    }

    fun stop() = controller.unbind()


    /**
     * Return 검지 Position
     */
    private fun onPoseDetected(pose: Pose) {
        val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)

        val viewWidth = previewView.width.toFloat()

        when {
            rightIndex != null -> {
                val xPx = rightIndex.position.x
                val normX = (xPx / viewWidth).coerceIn(0f, 1f)
                Log.d(TAG, "오른 손 검 지 감 지 정 규 화 된 X: $normX")
            }
            else -> {
                Log.d(TAG, "감지 못했엉!")
            }
        }
    }
}