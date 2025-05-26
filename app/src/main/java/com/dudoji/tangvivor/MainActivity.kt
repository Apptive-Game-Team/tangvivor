package com.dudoji.tangvivor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
import com.dudoji.tangvivor.matching.activity.RoomListActivity
import com.dudoji.tangvivor.repository.UserRepository
import com.dudoji.tangvivor.ui.theme.TangvivorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    lateinit var loginEditText: EditText
    lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginEditText = findViewById(R.id.login)
        loginButton = findViewById(R.id.login_button)

        loginButton.setOnClickListener {
            val loginText = loginEditText.text.toString()
            if (loginText.isNotEmpty()) {
                lifecycleScope.launch {
                    UserRepository.login(loginText)
                    val intent = Intent(this@MainActivity, RoomListActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}