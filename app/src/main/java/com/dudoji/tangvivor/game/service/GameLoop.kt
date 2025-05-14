package com.dudoji.tangvivor.game.service

class GameLoop {

    val handler = android.os.Handler()
    lateinit var runnable: Runnable

    companion object {
        private const val FRAME_RATE = 10 // frames per second
        private const val FRAME_TIME = 1000 / FRAME_RATE // milliseconds per frame
    }

    fun startGameLoop(update: () -> Unit) {
        runnable = object : Runnable {
            override fun run() {
                update()
                handler.postDelayed(this, FRAME_TIME.toLong())
            }
        }
        handler.post(runnable)
    }

    fun stopGameLoop() {
        handler.removeCallbacks(runnable)
    }
}