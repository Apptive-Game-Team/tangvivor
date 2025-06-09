package com.dudoji.tangvivor.game.service

import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dudoji.tangvivor.game.entity.Master
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

open class PlayerController(val master: Master, val player: ImageView, val constraintLayout: ConstraintLayout, val sessionId: String) {
    var frameWidth : Int = 0
    var playerWidth : Int = 0
    val db = FirebaseFirestore.getInstance()

    init {
        constraintLayout.post{
            // Get the width of the FrameLayout after it has been laid out
            frameWidth = constraintLayout.width
            playerWidth = player.width
        }
    }

    protected fun updateViewX(x: Float) {
        val newX = x * (frameWidth - playerWidth)
        player.x = (newX + player.x) / 2
    }

    // x range (0, 1)
    fun setX(x: Float) {
        updateViewX(x)

        db.collection("sessions")
            .document(sessionId)
            .update(if (master == Master.User1) "user1X" else "user2X", x)
    }

    fun onAttacked(damage: Long) {
        db.collection("sessions")
            .document(sessionId)
            .update(if (master == Master.User1) "user1Hp" else "user2Hp", FieldValue.increment(-damage))
    }
}