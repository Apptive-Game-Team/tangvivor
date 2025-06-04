package com.dudoji.tangvivor.game.entity

import com.dudoji.tangvivor.DEFAULT_HP


data class Session(var user1X: Float, var user2X: Float, var user1Hp: Int = DEFAULT_HP , var user2Hp: Int = DEFAULT_HP) {
    constructor() : this(0.5f, 0.5f)
}
