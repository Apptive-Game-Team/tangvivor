package com.dudoji.tangvivor.game

import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.camera.view.PreviewView
import com.dudoji.tangvivor.BaseDrawerActivity
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

class GameActivity : BaseDrawerActivity(), OnFacePositionListener {
    lateinit var playerController : PlayerController
    lateinit var enemyController : EnemyController

    lateinit var playerHpBar: ProgressBar
    lateinit var enemyHpBar: ProgressBar

    // Camera Setting
    private lateinit var previewView: PreviewView
    private lateinit var faceDetector: FaceDetector

    private lateinit var sessionId: String
    val gameLoop : GameLoop = GameLoop()
    var me by Delegates.notNull<Int>()
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContent(R.layout.activity_game)

        // Camera Setting
        previewView = findViewById(R.id.previewView)
        faceDetector = FaceDetector(
            context = this,
            lifecycleOwner = this,
            previewView = previewView,
            listener = this
        )

        playerHpBar = findViewById(R.id.player_hp_bar)
        enemyHpBar = findViewById(R.id.enemy_hp_bar)

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

        gameLoop.startGameLoop {
            db.collection("sessions")
                .document(sessionId)
                .get()
                .addOnSuccessListener { document ->
                    val session = document.toObject(Session::class.java)
                    if (session != null) {
                        enemyController.update(session)
                        updateHpBars(session)
                    }
                }
        }
    }

    fun updateHpBars(session: Session) {
        if (me == 1) {
            playerHpBar.progress = session.user1Hp.toInt()
            enemyHpBar.progress = session.user2Hp.toInt()
        } else {
            playerHpBar.progress = session.user2Hp.toInt()
            enemyHpBar.progress = session.user1Hp.toInt()
        }
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