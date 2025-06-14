package com.dudoji.tangvivor.game.activity

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.camera.view.PreviewView
import com.dudoji.tangvivor.BaseDrawerActivity
import com.dudoji.tangvivor.DEFAULT_HP
import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.game.camera.OnFacePositionListener
import com.dudoji.tangvivor.game.entity.Master
import com.dudoji.tangvivor.game.entity.Session
import com.dudoji.tangvivor.game.service.CombinedDetector
import com.dudoji.tangvivor.game.service.EnemyController
import com.dudoji.tangvivor.game.service.GameLoop
import com.dudoji.tangvivor.game.service.GunController
import com.dudoji.tangvivor.game.service.PlayerController
import com.dudoji.tangvivor.game.service.ResultHandler
import com.dudoji.tangvivor.repository.GameRepository
import com.dudoji.tangvivor.repository.ImageRespository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates
import kotlinx.coroutines.*

class GameActivity : BaseDrawerActivity(), OnFacePositionListener {
    lateinit var playerController : PlayerController
    lateinit var enemyController : EnemyController

    lateinit var playerPoint : PlayerController
    lateinit var enemyPoint : EnemyController

    lateinit var playerHpBar: ProgressBar
    lateinit var enemyHpBar: ProgressBar

    lateinit var playerGun: GunController
    lateinit var enemyGun: GunController

    // Camera Setting
    private lateinit var previewView: PreviewView
    private lateinit var combinedDetector: CombinedDetector

    private lateinit var sessionId: String
    private lateinit var sessionSaver: Session

    // Sound Setting
    private lateinit var soundPool: SoundPool
    private var shootSoundId: Int = 0

    val gameLoop : GameLoop = GameLoop()
    var me by Delegates.notNull<Int>()
    val db = FirebaseFirestore.getInstance()

    val resultHandler: ResultHandler = ResultHandler()

    var lastHp = DEFAULT_HP

    // blink Variable
    private var isBlinking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContent(R.layout.activity_game)

        me = intent.getIntExtra("me", -1)
        sessionId = intent.getStringExtra("roomName")!!

        playerController = PlayerController(
            if (me == 1) Master.User1 else Master.User2,
            findViewById<ImageView>(R.id.player),
            findViewById(R.id.game_frame_layout),
            sessionId,
            false
        )

        playerPoint = PlayerController(
            if (me == 1) Master.User1 else Master.User2,
            findViewById<ImageView>(R.id.playerPointer),
            findViewById(R.id.game_frame_layout),
            sessionId,
            true
        )

        playerGun = GunController(
            if (me == 1) Master.User1 else Master.User2,
            findViewById<ImageView>(R.id.playerGun),
            findViewById(R.id.game_frame_layout),
            sessionId,
            true
        )

        if (ImageRespository.imageUri != null)
            findViewById<ImageView>(R.id.player).setImageURI(ImageRespository.imageUri)

        // Camera Setting
        previewView = findViewById(R.id.previewView)

        playerHpBar = findViewById(R.id.player_hp_bar)
        enemyHpBar = findViewById(R.id.enemy_hp_bar)

        me = intent.getIntExtra("me", -1)
        sessionId = intent.getStringExtra("roomName")!!

       combinedDetector = CombinedDetector(
           context = this,
           lifecycleOwner = this,
           previewView = previewView,
           faceListener = ::onFacePosition,
           handListener = ::onPoseDetected,
           shootListener = ::onShootDetected
       )


        enemyController = EnemyController(
            if (me == 2) Master.User1 else Master.User2,
            findViewById<ImageView>(R.id.enemy),
            findViewById(R.id.game_frame_layout),
            sessionId,
            false
        )

        enemyPoint = EnemyController(
            if (me == 2) Master.User1 else Master.User2,
            findViewById<ImageView>(R.id.enemyPointer),
            findViewById(R.id.game_frame_layout),
            sessionId,
            true
        )

        enemyGun = GunController(
            if (me == 2) Master.User1 else Master.User2,
            findViewById<ImageView>(R.id.enemyGun),
            findViewById(R.id.game_frame_layout),
            sessionId,
            true
        )

        sessionSaver = Session(
            if (me == 1) Master.User1
            else Master.User2
        )


        // ============ Sound Part =============
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        shootSoundId = soundPool.load(this, R.raw.tang_sound, 1)

        gameLoop.startGameLoop {
            db.collection("sessions")
                .document(sessionId)
                .get()
                .addOnSuccessListener { document ->
                    val session = document.toObject(Session::class.java)
                    if (session != null) {
                        enemyController.update(session)
                        enemyPoint.update(session)
                        enemyGun.updateRotate(enemyPoint)
                        enemyGun.updateLocation(enemyController)
                        updateHpBars(session)

                        val currentHp = getMyHp()
                        if (lastHp > currentHp) {
                            hitBlinkImageView(playerController.player)
                            playTangAnimation(enemyGun.player)

                            lastHp = currentHp
                        }

                        if (session.getTang(me)) {
                            playTangAnimation(enemyGun.player)
                            playShootSound()
                        }

                        if (resultHandler.checkResult(this, session)) {
                            gameLoop.stopGameLoop()
                            return@addOnSuccessListener
                        }

                        db.collection("sessions")
                            .document(sessionId)
                            .update(sessionSaver.toMap(session))

                        sessionSaver.initialize()
                    }
                }
        }
    }

    fun getMyHp(): Int {
        return if (me == 1) sessionSaver.user1Hp.toInt() else sessionSaver.user2Hp.toInt()
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
        runOnUiThread {
            playerController.setX(normX, sessionSaver)
            playerGun.updateLocation(playerController)
        }
    }

    private fun onPoseDetected(normX: Float) {
        runOnUiThread {
            playerPoint.setX(normX, sessionSaver)
            playerGun.updateRotate(playerPoint)
        }
    }

    private fun onShootDetected() {
        Log.d("HandHand", "Sex")
        val playerPointX = playerPoint.player.x
        val playerPointY = playerPoint.player.y
        val playerPointWidth = playerPoint.player.width
        val playerPointHeight = playerPoint.player.height

        val enemyX = enemyController.player.x
        val enemyY = enemyController.player.y
        val enemyWidth = enemyController.player.width
        val enemyHeight = enemyController.player.height

        // SOUND
        playShootSound()

        sessionSaver.setTang(me)

        if (
            playerPointX >= enemyX && playerPointX + playerPointWidth <= enemyX + enemyWidth &&
            playerPointY >= enemyY && playerPointY + playerPointHeight <= enemyY + enemyHeight
        ) {
            enemyController.onAttacked(10, sessionSaver)
            hitBlinkImageView(enemyController.player)
            playTangAnimation(playerGun.player)
        }
    }

    private fun playTangAnimation(imageView: ImageView) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                imageView.setImageResource(R.drawable.weapons_tanging)
                delay(500)
                imageView.setImageResource(R.drawable.weapons)
            }
        }
    }

    private fun hitBlinkImageView(imageView: ImageView) {
        if (isBlinking) return
        isBlinking = true;
        GlobalScope.launch {
            val endTime = System.currentTimeMillis() + 1000

            var isVisible = true

            while (System.currentTimeMillis() < endTime) {
                withContext(Dispatchers.Main) {
                    imageView.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
                }
                isVisible = !isVisible
                delay(100)  // 500ms마다 깜빡이기 (1초 동안 2번 깜빡임)
            }

            withContext(Dispatchers.Main) {
                imageView.visibility = View.VISIBLE
            }
            isBlinking = false
        }
    }

    override fun onResume() {
        super.onResume()
        combinedDetector.start()
    }

    override fun onPause() {
        super.onPause()
        combinedDetector.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameLoop.stopGameLoop()
        soundPool.release()
        GameRepository.quitGame()
    }

    // Shoot Sound
    fun playShootSound() {
        soundPool.play(shootSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }
}