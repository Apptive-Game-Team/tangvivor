package com.dudoji.tangvivor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.dudoji.tangvivor.game.GameActivity
import com.dudoji.tangvivor.login.service.LoginService
import com.dudoji.tangvivor.matching.activity.RoomListActivity
import com.dudoji.tangvivor.repository.UserRepository
import com.dudoji.tangvivor.ui.theme.TangvivorTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    lateinit var loginButton: ImageButton

    lateinit var loginService: LoginService

    private val REQ_ONE_TAP = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginButton = findViewById(R.id.login_button)

        loginButton.setOnClickListener {
            loginService.signIn{
                lifecycleScope.launch {
                    val intent = Intent(this@MainActivity, RoomListActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        loginService = LoginService(this)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {
            loginService.onResult(data)
        }
    }
}