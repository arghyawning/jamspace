package com.meerkat.jamspace

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import android.widget.EditText
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.meerkat.jamspace.Opening.Companion.token
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val regButton = findViewById<Button>(R.id.btn_register)
        val backButton = findViewById<Button>(R.id.btn_back)

        val db = Firebase.firestore
        val usersDb = db.collection("users")


        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Enable or disable the button based on the result
                regButton.isEnabled =
                    TextUtils.isEmpty(emailEditText.text).not() && TextUtils.isEmpty(
                        passwordEditText.text
                    ).not()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)

        val auth = FirebaseAuth.getInstance()
        var imageurl = ""
        regButton.isEnabled = false

        regButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(ContentValues.TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        if (user != null) {
                            getUserPhotoArt(object : Register.UserPhotoCallback {
                                override fun onSuccess(imgUrl: String) {
                                    println("omg this is the  ${imgUrl}")
                                    imageurl = imgUrl
                                    val createUser = hashMapOf<String, Any>(
                                        "userId" to Opening.userid,
                                        "email" to email,
                                        "starredSongs" to emptyList<String>(),
                                        "userImgUrl" to imageurl,
                                        "token" to Opening.token
                                    )
                                    val createuser = usersDb.add(createUser)
                                    createuser.addOnSuccessListener { documentReference ->
                                        Log.d(
                                            "ts sucks",
                                            "DocumentSnapshot added with ID: ${documentReference.id}"
                                        )
                                    }
                                    createuser.addOnFailureListener { e ->
                                        Log.w("ts sucks", "Error adding document", e)
                                    }
                                }

                                override fun onError(error: String) {
                                    println("$error")
                                }
                            })

                            Opening.userid = user.uid
                            println("userrr =  ${Opening.userid}")


                        }
                        val intent = Intent(this, SongScreen::class.java)
                        startActivity(intent)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, SongScreen::class.java)
            startActivity(intent)
        }


    }

    interface UserPhotoCallback {
        fun onSuccess(imgUrl: String)
        fun onError(error: String)
    }

    private fun getUserPhotoArt(callback: UserPhotoCallback): String {
        var imgurl = ""

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/")
            .addHeader("Authorization", "Bearer ${Opening.token}")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("RRequest failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                println("pic data: ${responseData}")

                val jsonObject = JSONObject(responseData)
                val imagesArray = jsonObject.getJSONArray("images")
                if (imagesArray.length() > 0) {
                    val imageUrl = imagesArray.getJSONObject(0).getString("url")
                    println("pic url: ${imageUrl}")
                    callback.onSuccess(imageUrl)
                } else {
                    println("no prof pic")
                }
            }
        })
        println(imgurl)
        return imgurl
    }
}

