package com.dudoji.tangvivor.game.service

import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.game.activity.GameActivity
import com.dudoji.tangvivor.game.activity.ResultActivity
import com.dudoji.tangvivor.game.activity.ResultType
import com.dudoji.tangvivor.game.entity.Session
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ResultHandler {
    fun checkResult(gameActivity: GameActivity, session: Session): Boolean {
        if (session.user1Hp <= 0 || session.user2Hp <= 0) {
            var result: ResultType = getResultType(session, gameActivity.me)

            if (result == ResultType.WIN) {
                GlobalScope.launch {
                    updateScore(gameActivity)
                }
            }

            val intent = ResultActivity.getActivityIntent(gameActivity, result)
            gameActivity.startActivity(intent)
            return true
        }

        return false
    }

    suspend fun updateScore(gameActivity: GameActivity) {
        val client = PlayGames.getLeaderboardsClient(gameActivity)
        var score: Long = client
            .loadCurrentPlayerLeaderboardScore(gameActivity.getString(R.string.score_leaderboard_id),
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC
            ).await().get()?.rawScore ?: 0L
        client.submitScore(gameActivity.getString(R.string.score_leaderboard_id),
                score + 1
        )
    }

    fun getResultType(session: Session, me: Int): ResultType {
        return when {
            session.user1Hp <= 0 && session.user2Hp <= 0 -> ResultType.DRAW
            session.user1Hp <= 0 && me == 1 -> ResultType.LOSE
            session.user2Hp <= 0 && me == 2 -> ResultType.LOSE
            session.user1Hp <= 0 && me == 2 -> ResultType.WIN
            session.user2Hp <= 0 && me == 1 -> ResultType.WIN
            else -> ResultType.DRAW
        }
    }
}