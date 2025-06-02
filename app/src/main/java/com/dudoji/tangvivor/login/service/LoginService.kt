package com.dudoji.tangvivor.login.service

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.matching.entity.User
import com.dudoji.tangvivor.repository.UserRepository
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class LoginService(val activity: ComponentActivity){

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    var onSuccess: () -> Unit = {}
    private val REQ_ONE_TAP = 2

    init {
        oneTapClient = Identity.getSignInClient(activity)

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(activity, R.string.oauth_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }

    fun signIn(onSuccess: () -> Unit) {
        this.onSuccess = onSuccess
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(activity) { result ->
                try {
                    activity.startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        REQ_ONE_TAP,
                        null,
                        0,
                        0,
                        0
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
            .addOnFailureListener(activity) { e ->
                e.printStackTrace()
            }
    }

    fun onResult(data: Intent?) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            val email = credential.id
            val displayName = credential.displayName

            if (idToken != null) {
                Log.d("GoogleSignIn", "ID Token: $idToken")
                Log.d("GoogleSignIn", "Email: $email")
                Log.d("GoogleSignIn", "Name: $displayName")

                val user = User()
                user.id = email
                user.name = displayName ?: "Unknown"
                user.score = 0 // Default score, TODO can be updated later

                activity.lifecycleScope.launch {
                    UserRepository.saveUser(user)
                    UserRepository.me = user
                    onSuccess()
                }

            } else {
                Log.e("GoogleSignIn", "No ID token!")
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Sign-in failed", e)
        }
    }
}