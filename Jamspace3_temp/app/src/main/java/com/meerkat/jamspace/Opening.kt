package com.meerkat.jamspace

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import java.io.IOException
import com.google.firebase.ktx.Firebase

class Opening : AppCompatActivity() {
    companion object {
        var token:String =""
//        var dp=""
        var userid = ""

    }

    private lateinit var auth: FirebaseAuth
//    val auth=FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening)

        // Initialize Firebase Auth
//        auth = Firebase.auth
    }

//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        println("lets check $currentUser")
////        if(currentUser != null){
////            val intent = Intent(this, SongScreen::class.java)
////            startActivity(intent)
////        }
//    }

    fun goToRegister(view: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    fun goToSpotifyAuth(view: View) {
        val builder =
            AuthorizationRequest.Builder(
                MainActivity.CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                MainActivity.REDIRECT_URI
            )

        builder.setScopes(
            arrayOf(
                "streaming",
                "user-read-currently-playing",
                "user-read-email"
            )
        )
        builder.setShowDialog(true)
        val request = builder.build()

        AuthorizationClient.openLoginActivity(this, MainActivity.REQUEST_CODE, request)
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request.toUri().toString()))
//        startActivity(intent)
    }

    fun goToLogin(view: View) {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MainActivity.REQUEST_CODE) {
            println("works!")
            val response = AuthorizationClient.getResponse(resultCode, data)
//            println(response.accessToken)
            if (response.type == AuthorizationResponse.Type.TOKEN) {
                println("yay?")
                token = response.accessToken
                println("token: $token")
                val intent = Intent(this, Register::class.java)
                intent.putExtra("access_token", response.accessToken)
                startActivity(intent)
                finish()
            } else if (response.type == AuthorizationResponse.Type.ERROR) {
                Log.e("Opening", "Authorization error: ${response.error}")
            }
        }

    }

}
