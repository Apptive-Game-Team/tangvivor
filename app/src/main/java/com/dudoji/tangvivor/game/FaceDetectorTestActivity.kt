package com.dudoji.tangvivor.game

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.camera.view.PreviewView
import com.dudoji.tangvivor.game.camera.OnFacePositionListener
import com.dudoji.tangvivor.game.service.FaceDetector
import com.dudoji.tangvivor.R


class FaceDetectorTestActivity : ComponentActivity(), OnFacePositionListener {

    private lateinit var previewView: PreviewView
    private lateinit var player: ImageView
    private lateinit var faceDetector: FaceDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_test)

        previewView = findViewById(R.id.previewView)
        player      = findViewById(R.id.playerImage)

        faceDetector = FaceDetector(
            context        = this,
            lifecycleOwner = this,
            previewView    = previewView,
            listener       = this            // 콜백 구현체
        )
    }

    override fun onResume() {
        super.onResume()
        faceDetector.start()
    }

    override fun onPause() {
        super.onPause()
        faceDetector.stop()
    }
    override fun onFacePosition(normX: Float) {
        val halfWidth = previewView.width/2f
        player.translationX = normX * halfWidth
    }


}