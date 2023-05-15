package com.meerkat.jamspace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.spotify.sdk.android.auth.AuthorizationResponse
import java.io.IOException

class MainActivity : AppCompatActivity() {
    companion object {
        val CLIENT_ID = "31e09c4520624facb7ab05d0447aa48d"
        val REQUEST_CODE = 1337
        val REDIRECT_URI = "jamspace://callback"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        println("Hi2 and intent: $intent")
//        val uri: Uri? = intent?.data
//        println("uri" + uri)
//        if (uri != null) {
//            val response: AuthorizationResponse = AuthorizationResponse.fromUri(uri)
//            println("Hi6")
//            when (response.type) {
//                // Response was successful and contains auth token
//                AuthorizationResponse.Type.TOKEN -> {
//                    println("Hi3")
//                    MainActivity.token = response.accessToken
//                    // Handle successful response
//                    Log.d("MainActivity", "Received token: ${MainActivity.token}")
//                    println("Received token: ${MainActivity.token}")
//                }
//
//                // Auth flow returned an error
//                AuthorizationResponse.Type.ERROR -> {
//                    // Handle error response
//                    Log.e("MainActivity", "Authorization error")
//                    println("Hi4")
//                }
//
//                // Most likely auth flow was cancelled
//                else -> {
//                    // Handle other cases
//                    Log.d("MainActivity", "Authorization cancelled")
//                    println("Hi5")
//                }
//            }
//        }
//    }
}