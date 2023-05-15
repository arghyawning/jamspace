package com.meerkat.jamspace

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        println("hihihi: ${Opening.token}")

        val editTextEmail = findViewById<EditText>(R.id.email)
        val editTextPassword = findViewById<EditText>(R.id.password)
        val logButton = findViewById<Button>(R.id.btn_login)
        val backButton = findViewById<Button>(R.id.btn_back_l)

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Enable or disable the button based on the result
                logButton.isEnabled =
                    TextUtils.isEmpty(editTextEmail.text).not() && TextUtils.isEmpty(
                        editTextPassword.text
                    ).not()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        editTextEmail.addTextChangedListener(textWatcher)
        editTextPassword.addTextChangedListener(textWatcher)

        val auth = FirebaseAuth.getInstance()

        val db = Firebase.firestore
        val usersDb = db.collection("users")

        logButton.isEnabled = false

        logButton.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(ContentValues.TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        if (user != null) {
                            Opening.userid = user.uid
                        }

                        Log.d("Logged", "Logged in")

                        usersDb.whereEqualTo("userId", Opening.userid)
                            .get()
                            .addOnSuccessListener { documents ->
                                println("umm $documents")
                                if (!documents.isEmpty) {
                                    val refs = documents.first()
                                    val tok = refs.get("token") as String
                                    println("we here: $tok")
                                    Opening.token = tok

//                                    Opening.dp=refs.get("userImgUrl") as String

                                    val intent = Intent(this, SongScreen::class.java)
                                    startActivity(intent)
                                }

                            }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }

        backButton.setOnClickListener {
            val intent = Intent(this, Opening::class.java)
            startActivity(intent)
        }


    }

}