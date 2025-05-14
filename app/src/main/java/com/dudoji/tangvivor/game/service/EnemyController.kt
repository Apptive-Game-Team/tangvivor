package com.dudoji.tangvivor.game.service

import android.widget.FrameLayout
import android.widget.ImageView
import com.dudoji.tangvivor.game.entity.Master
import com.dudoji.tangvivor.game.entity.Session

val TEST_SESSION_ID = "CR6vF7GlvPWvAHOJolJs"

class EnemyController : PlayerController {
    constructor(master: Master, player: ImageView, frameLayout: FrameLayout) : super(master, player, frameLayout) { }

    public fun update() {
        db.collection("sessions")
            .document(TEST_SESSION_ID)
            .get()
            .addOnSuccessListener { document ->
                val session = document.toObject(Session::class.java)
                if (session != null) {
                    updateViewX(
                        if (master == Master.User1) session.user1X else session.user2X
                    )
                }
            }
    }

}