package com.dudoji.tangvivor.game.service

import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dudoji.tangvivor.game.entity.Master
import com.dudoji.tangvivor.game.entity.Session

val TEST_SESSION_ID = "CR6vF7GlvPWvAHOJolJs"

class EnemyController : PlayerController {

    constructor(master: Master, player: ImageView, constraintLayout: ConstraintLayout, roomName: String, isPointer: Boolean) : super(master, player, constraintLayout, roomName, isPointer) { }

    fun update(session: Session) {
        updateViewX(
            if (isPointer) {
                if (master == Master.User1) session.user1Point else session.user2Point
            }
            else {
                if (master == Master.User1) session.user1X else session.user2X
            }
        )
    }
}