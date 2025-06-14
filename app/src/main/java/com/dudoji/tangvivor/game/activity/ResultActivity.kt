package com.dudoji.tangvivor.game.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.dudoji.tangvivor.BaseDrawerActivity
import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.matching.activity.RoomListActivity
import com.google.android.gms.games.PlayGames

enum class ResultType {
    WIN, LOSE, DRAW
}

class ResultActivity: BaseDrawerActivity() {
    val resultType: ResultType by lazy {
        ResultType.valueOf(intent.getStringExtra("resultType") ?: ResultType.DRAW.name)
    }

    companion object {
        fun getActivityIntent(context: Context, resultType: ResultType): Intent {
            val intent = Intent(context, ResultActivity::class.java)
            intent.putExtra("resultType", resultType.name)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContent(R.layout.activity_result)

        val titleTextView = findViewById<TextView>(R.id.result_status)

        when (resultType) {
            ResultType.WIN -> {
                titleTextView.setTextColor(Color.parseColor("#4CAF50"))
                titleTextView.text = "승리"
            }
            ResultType.LOSE -> {
                titleTextView.setTextColor(Color.parseColor("#F44336"))
                titleTextView.text ="패배"
            }
            ResultType.DRAW -> {
                titleTextView.setTextColor(Color.parseColor("#F44336"))
                titleTextView.text = "무승부"
            }
        }

        val closeButton = findViewById<Button>(R.id.close_button)
        closeButton.setOnClickListener {
            val intent = Intent(this, RoomListActivity::class.java)
            startActivity(intent)
        }

        val leaderBoardButton = findViewById<Button>(R.id.leaderboard_button)
        leaderBoardButton.setOnClickListener {
            PlayGames.getLeaderboardsClient(this)
                .getLeaderboardIntent(getString(R.string.score_leaderboard_id))
                .addOnSuccessListener(this){intent ->
                    startActivityForResult(intent, 1001)
                }
                .addOnFailureListener(this) { e ->
                    Log.e("MatchingSystem", "Failed to get leaderboard intent", e)
                    Toast.makeText(this, "Failed to open leaderboard", Toast.LENGTH_SHORT).show()
                }
        }
    }
}