package com.dudoji.tangvivor.game.service

import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dudoji.tangvivor.game.entity.Master
import com.dudoji.tangvivor.game.entity.Session
import kotlin.math.atan2
import java.lang.Math

class GunController: PlayerController {
    constructor(master: Master, player: ImageView, constraintLayout: ConstraintLayout, roomName: String, isPointer: Boolean) : super(master, player, constraintLayout, roomName, isPointer) { }

    fun updateRotate(pointer : PlayerController) {
        val weaponCenterX = this.player.x + this.player.width / 2f
        val weaponCenterY = this.player.y + this.player.height / 2f

        // 2. 포인터(조준점)와 총 중심점 사이의 벡터 계산
        val deltaX = pointer.player.x - weaponCenterX
        val deltaY = pointer.player.y - weaponCenterY

        var angleDegrees = Math.toDegrees(atan2(deltaY, deltaX).toDouble()).toFloat()

        player.rotation = angleDegrees + 270
    }

    fun updateLocation(player : PlayerController) {
        this.player.x = player.player.x
    }
}
