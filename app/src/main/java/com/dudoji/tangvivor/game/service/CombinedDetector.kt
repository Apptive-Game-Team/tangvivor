package com.dudoji.tangvivor.game.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.framework.image.BitmapImageBuilder
import kotlin.math.abs
import kotlinx.coroutines.*

class CombinedDetector(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val faceListener: (Float) -> Unit,
    private val handListener: (Float) -> Unit,
    private val shootListener: () -> Unit,
) {
    companion object {
        private const val TAG = "CombinedDetector"
        private const val MP_HAND_MODEL = "hand_landmarker.task"
        private const val MP_FACE_MODEL = "face_landmarker.task"
        private const val MAX_NUM_HANDS = 1
        private const val MIN_DETECTION_CONFIDENCE = 0.5f
    }

    // CameraX
    private val controller = LifecycleCameraController(context).apply {
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    }

    private lateinit var handLandmarker : HandLandmarker
    private lateinit var faceLandmarker : FaceLandmarker

    private var isFiring = false;
    private fun fireWithDelay() {
        Log.d("HandHand", "fireWithDelay")
        if (isFiring) return;
        isFiring = true
        shootListener()
        GlobalScope.launch {
            delay(1000)
            isFiring = false
        }
    }

    private fun setupDetectors() {
        var prevY: Float? = null
        val speedThreshold = 0.05f
        // 손 먼저
        val handBaseOptions = BaseOptions.builder()
            .setModelAssetPath(MP_HAND_MODEL)
            .build()

        val handOptions = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(handBaseOptions)
            .setMinHandDetectionConfidence(MIN_DETECTION_CONFIDENCE)
            .setMinTrackingConfidence(MIN_DETECTION_CONFIDENCE)
            .setNumHands(MAX_NUM_HANDS)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ ->
                result.landmarks().firstOrNull()?.let { landmarks ->
                    if (landmarks.size > 8) {
                        val wristY = landmarks[0].y().coerceIn(0f, 1f)
                        val fingertipY = landmarks[8].y().coerceIn(0f, 1f)

                        prevY?.let { prev ->
                            val diff = abs(prev - fingertipY)
                            if (diff > speedThreshold && fingertipY < wristY) {
                                fireWithDelay()
                            }
                        }
                        prevY = fingertipY
                        handListener(landmarks[8].x().coerceIn(0f, 1f))
                    }
                }
            }
            .setErrorListener { error -> Log.e(TAG, "Hand Error: $error") }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, handOptions)

        // 2. Face Landmarker 설정
        val faceBaseOptions = BaseOptions.builder()
            .setModelAssetPath(MP_FACE_MODEL)
            .build()

        val faceOptions = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(faceBaseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ ->
                result.faceLandmarks().firstOrNull()?.let { landmarks ->
                    if (landmarks.size > 1) { // 코 근처 랜드마크
                        val centerX = landmarks[1].x()
                        val viewW = previewView.width.toFloat()
                        val pixelX = centerX * viewW
                        val normX = centerX / viewW
                        faceListener(landmarks[1].x())
                    }
                }
            }
            .setErrorListener { error -> Log.e(TAG, "Face Error: $error") }
            .build()

        faceLandmarker = FaceLandmarker.createFromOptions(context, faceOptions)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        previewView.controller = controller
        controller.bindToLifecycle(lifecycleOwner)

        controller.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            ImageAnalysis.Analyzer { imageProxy ->
                val mediaImage = imageProxy.image ?: run {
                    imageProxy.close()
                    return@Analyzer
                }

                // bimtmap 치환
                val bitmap = imageProxy.toBitmap()
                val rotatedBitmap = rotateBitmapForFrontCamera(bitmap, imageProxy.imageInfo.rotationDegrees)
                val mpImage = BitmapImageBuilder(rotatedBitmap).build()

                val timestamp = imageProxy.imageInfo.timestamp
                handLandmarker.detectAsync(mpImage, timestamp)
                faceLandmarker.detectAsync(mpImage, timestamp)

                imageProxy.close()
            }
        )
    }
    fun start() {
        setupDetectors()
        startCamera()
    }
    fun stop() {
        controller.unbind()
        handLandmarker.close()
        faceLandmarker.close()
    }


    private fun rotateBitmapForFrontCamera(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
            postScale(-1f, 1f) // 프론트 카메라 미러 효과
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

//    private val faceDetector = FaceDe
//    @OptIn(ExperimentalGetImage::class)
//    fun start() {
//        previewView.controller = controller
//        controller.bindToLifecycle(lifecycleOwner)
//
//        controller.setImageAnalysisBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//
//        // Create FaceDetector, PoseDetector
//        val faceDetector = FaceDetection.getClient(
//            FaceDetectorOptions.Builder()
//                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
//                .build()
//        )
//        val poseDetector = PoseDetection.getClient(
//            PoseDetectorOptions.Builder()
//                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
//                .build()
//        )
//
//        val analyzer = ImageAnalysis.Analyzer {imageProxy ->
//            val mediaImage = imageProxy.image ?: run {
//                imageProxy.close()
//                return@Analyzer
//            }
//
//            val image = InputImage.fromMediaImage(
//                mediaImage,
//                imageProxy.imageInfo.rotationDegrees
//            )
//
//
//            var completedCount = 0
//            val totalTasks = 2
//
//            fun tryClose() {
//                completedCount++
//                if (completedCount == totalTasks) {
//                    imageProxy.close()
//                }
//            }
////            val handler = android.os.Handler(android.os.Looper.getMainLooper())
////            val closeRunnable = Runnable {
////                imageProxy.close()
////                Log.d("SEX", "TIME OUT IMAGE PROXY CLOSE")
////            }
////
////            handler.postDelayed(closeRunnable, 50L)
//
//            faceDetector.process(image)
//                .addOnSuccessListener { faces -> // When Success return 0 ~ 1 val
//                    faces.firstOrNull()?.let { face ->
//                        val normX = face.boundingBox.centerX().toFloat() / previewView.width.toFloat()
//                        faceListener(normX.coerceIn(0f, 1f))
//                    }
//                }
//                .addOnCompleteListener { tryClose() }
//            poseDetector.process(image)
//                .addOnSuccessListener { pose ->
//                    pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)?.let { finger ->
//                        val normX = finger.position.x / previewView.width.toFloat()
//                        handListener(normX.coerceIn(0f, 1f))
//                    } ?: run {
//                        handListener(0f);
//                    }
//                }
//                .addOnCompleteListener { tryClose() }
//        }
//        controller.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
//    }
//
//    fun stop() = controller.unbind()
}