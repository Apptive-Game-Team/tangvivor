package com.dudoji.tangvivor.game

import android.widget.FrameLayout
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.game.camera.OnFacePositionListener
import com.dudoji.tangvivor.game.entity.Master
import com.dudoji.tangvivor.game.entity.Session
import com.dudoji.tangvivor.game.service.EnemyController
import com.dudoji.tangvivor.game.service.FaceDetector
import com.dudoji.tangvivor.game.service.GameLoop
import com.dudoji.tangvivor.game.service.PlayerController
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.properties.Delegates

class GameActivity : ComponentActivity(), OnFacePositionListener {

    lateinit var seekBar : SeekBar
    lateinit var playerController : PlayerController
    lateinit var enemyController : EnemyController

    // Camera Setting
    private lateinit var previewView: PreviewView
    private lateinit var faceDetector: FaceDetector
    private lateinit var sessionId: String
    val gameLoop : GameLoop = GameLoop()
    var me by Delegates.notNull<Int>()
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Camera Setting
        previewView = findViewById(R.id.previewView)
        faceDetector = FaceDetector(
            context = this,
            lifecycleOwner = this,
            previewView = previewView,
            listener = this
        )
        
        me = intent.getIntExtra("me", -1)
        sessionId = intent.getStringExtra("roomName")!!

        playerController = PlayerController(
            if (me == 1) Master.User1 else Master.User2,
            findViewById<ImageView>(R.id.player),
            findViewById(R.id.game_frame_layout),
            sessionId
        )

        enemyController = EnemyController(
            if (me == 2) Master.User1 else Master.User2,
            findViewById<ImageView>(R.id.enemy),
            findViewById(R.id.game_frame_layout),
            sessionId
        )

        setSeekBar()
        gameLoop.startGameLoop {
            db.collection("sessions")
                .document(sessionId)
                .get()
                .addOnSuccessListener { document ->
                    val session = document.toObject(Session::class.java)
                    if (session != null) {
                        enemyController.update(session)
                    }
                }
        }
    }

    fun setSeekBar() {
        seekBar = findViewById(R.id.seekBar)
        seekBar.max = 100
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Handle the progress change
                    // For example, update a TextView or perform some action based on the progress
                    val value = progress / 100.0f
                    playerController.setX(value.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Handle start of touch
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Handle stop of touch
            }
        })
    }

    // Camera Function
    override fun onFacePosition(normX: Float) {
        playerController.setX(normX)
    }

    override fun onResume() {
        super.onResume()
        faceDetector.start()
    }

    override fun onPause() {
        super.onPause()
        faceDetector.stop()
    }
}