package com.dudoji.tangvivor.game.entity

data class Session(val user1X: Float, val user2X: Float) {
    constructor() : this(0f, 0f)
}
