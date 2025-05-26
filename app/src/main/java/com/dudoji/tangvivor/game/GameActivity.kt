package com.dudoji.tangvivor.game

import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.game.entity.Master
import com.dudoji.tangvivor.game.service.EnemyController
import com.dudoji.tangvivor.game.service.GameLoop
import com.dudoji.tangvivor.game.service.PlayerController
import kotlin.properties.Delegates

class GameActivity : ComponentActivity() {

    lateinit var seekBar : SeekBar
    lateinit var playerController : PlayerController
    lateinit var enemyController : EnemyController

    val gameLoop : GameLoop = GameLoop()
    var me by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        me = intent.getIntExtra("me", -1)
        playerController = PlayerController(
            if (me == 1) Master.User1 else Master.User2,
            findViewById<ImageView>(R.id.player),
            findViewById(R.id.game_frame_layout)
        )

        enemyController = EnemyController(
            if (me == 2) Master.User1 else Master.User2,
            findViewById<ImageView>(R.id.enemy),
            findViewById(R.id.game_frame_layout),
            intent.getStringExtra("roomName")!!
        )

        setSeekBar()
        gameLoop.startGameLoop {
            enemyController.update()
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
}