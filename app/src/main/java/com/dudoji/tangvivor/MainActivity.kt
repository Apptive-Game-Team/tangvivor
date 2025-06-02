package com.dudoji.tangvivor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.dudoji.tangvivor.matching.activity.RoomListActivity
import com.dudoji.tangvivor.matching.entity.User
import com.dudoji.tangvivor.repository.UserRepository
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    lateinit var loginButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginButton = findViewById(R.id.login_button)
        val gamesSignInClient = PlayGames.getGamesSignInClient(this);
        gamesSignInClient.isAuthenticated.addOnCompleteListener{
            if (it.isSuccessful && it.result.isAuthenticated) {
                onSignInSuccess()
            } else {
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
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, RoomListActivity::class.java)
            startActivity(intent)
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