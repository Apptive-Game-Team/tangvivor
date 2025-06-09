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
import com.dudoji.tangvivor.game.camera.OnFacePositionListener
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

@Deprecated("No Use")
class FaceDetector(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val listener: OnFacePositionListener
) {

    // TAG
    companion object {
        private val TAG = "FaceDetector"
    }

    // CameraX
    private val controller = LifecycleCameraController(context).apply {
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    }

    fun start() {

        // Preview Binding
        previewView.controller = controller
        controller.bindToLifecycle(lifecycleOwner)

        // FaceDetection
        val detectorOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        val detectorClient = FaceDetection.getClient(detectorOptions)

        // Analyzer Connect
        val analyzer = MlKitAnalyzer(
            listOf(detectorClient),
            ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
            ContextCompat.getMainExecutor(context)
        ) { result ->
            val faces: List<Face>? = result.getValue(detectorClient)
            faces?.firstOrNull()?.let { face ->
                val centerX = face.boundingBox.centerX().toFloat()
                val viewW = previewView.width.toFloat()
                val normX = centerX / viewW
                listener.onFacePosition(normX.coerceIn(0f, 1f))
            }
        }

        controller.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            analyzer
        )
    }

    fun stop() = controller.unbind()
}