package com.dudoji.tangvivor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.dudoji.tangvivor.matching.activity.RoomListActivity
import com.dudoji.tangvivor.matching.entity.User
import com.dudoji.tangvivor.repository.UserRepository
import com.google.android.gms.games.PlayGames
import kotlinx.coroutines.launch

class MainActivity : BaseDrawerActivity() {
    val gamesSignInClient by lazy {
        PlayGames.getGamesSignInClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContent(R.layout.activity_main)

        checkSignInStatus()
    }

    private fun checkSignInStatus() {
        val gamesSignInClient = PlayGames.getGamesSignInClient(this);
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.isAuthenticated) {
                onSignInSuccess()
            } else {
                Log.d("MainActivity", "User is not authenticated")
                signIn()
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }
    }

    private fun signIn() {
        gamesSignInClient.signIn().addOnCompleteListener { signInTask ->
            if (signInTask.isSuccessful && signInTask.result.isAuthenticated) {
                onSignInSuccess()
            } else {
                signInTask.exception?.let { exception ->
                    exception.printStackTrace()
                }
            }
        }
    }

    private fun onSignInSuccess() {
        PlayGames.getPlayersClient(this).getCurrentPlayer().addOnSuccessListener { player ->
            val user = User(
                player.playerId,
                player.displayName,
                0
            )
            lifecycleScope.launch {
                Log.d("MainActivity", "User signed in: $user")
                UserRepository.saveUser(user)
                UserRepository.me = user

                val intent = Intent(this@MainActivity, RoomListActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
        }
    }
}