package com.dudoji.tangvivor.repository

import android.content.Context
import android.content.Intent
import com.dudoji.tangvivor.game.activity.GameActivity
import com.dudoji.tangvivor.game.entity.Session
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object GameRepository {
    val db = FirebaseFirestore.getInstance()
    val COLLECTION_NAME = "sessions"
    var isInGame: Boolean = false
    var currentSessionId: String = ""

    suspend fun saveGame(name: String): Boolean {
        return try {
            db.collection(COLLECTION_NAME).document(name).set(Session()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun enterGame(roomName: String, context: Context, me: Int) {
        isInGame = true
        currentSessionId = roomName
        val intent = Intent(context, GameActivity::class.java)
        intent.putExtra("roomName", roomName)
        intent.putExtra("me", me)
        context.startActivity(intent)
    }

    fun quitGame() {
        isInGame = false
        currentSessionId = ""
        // TODO: Implement game quitting logic
    }
}