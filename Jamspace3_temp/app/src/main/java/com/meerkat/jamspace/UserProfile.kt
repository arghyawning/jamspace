package com.meerkat.jamspace

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import android.view.Gravity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class UserProfile : AppCompatActivity() {
    val db = Firebase.firestore
    val usersDb = db.collection("users")
    val songDb=db.collection("songs")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val profilePicP = findViewById<ImageView>(R.id.profile_pic)
        profilePicP.setImageResource(R.drawable.profile_image)
        getUserPhotoArt(object : UserProfile.UserPhotoCallback {
            override fun onSuccess(imgUrl: String) {
                println("omg ${imgUrl}")
                runOnUiThread {
                    Glide.with(this@UserProfile)
                        .load(imgUrl)
                        .into(profilePicP)
                }

            }

            override fun onError(error: String) {
                println("$error")
            }
        })

        val user = findViewById<TextView>(R.id.user_name)
//        user.text = "fs"
        getUsername(object : UsernameCallback {
            override fun onSuccess(username: String) {
                println("in username ${username}")
                runOnUiThread {
                    user.text = username
                }
            }
            override fun onError(error: String) {
                println("$error")
            }
        })

        val followersCnt = findViewById<TextView>(R.id.followers_cnt)
//        followersCnt.text = "Followers: 500"
        getFollowersCount(object : FollowersCountCallback {
            override fun onSuccess(fwrno: String) {
                runOnUiThread {
                    followersCnt.text = "Followers: $fwrno"
                }
            }

            override fun onError(error: String) {
                println("$error")
            }
        })

        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)

        var numTexts = 20 // Set the number of texts here
        var starredSongs: List<String> = emptyList()
//        starredSongs=[]
        usersDb.whereEqualTo("userId", Opening.userid)
            .get()
            .addOnSuccessListener { documents ->
                println("umm $documents")
                if (!documents.isEmpty) {
                    val refs = documents.first()
                    val starredSongs = refs.get("starredSongs") as List<String>

//                    val textView = TextView(this)
//                    val textView2 = TextView(this)
//                    val imageView = ImageView(this)

                    for (song in starredSongs)
                    {
                        songDb.whereEqualTo("trackId", song)
                            .get()
                            .addOnSuccessListener{songs->
                                if (!songs.isEmpty)
                                {
                                    val s = songs.first()

                                    val textView = TextView(this)
                                    textView.layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    textView.textSize = 16f
                                    textView.setTextColor(resources.getColor(R.color.black))
                                    textView.setPadding(8, 8, 8, 8)
                                    //            textView.text = "Starred Songs $i" // Set the text here
                                    textView.text=s.getString("songName")
                                    textView.setTypeface(null, Typeface.BOLD)
                                    textView.gravity = Gravity.CENTER
                                    linearLayout.addView(textView)


                                    val textView2 = TextView(this)
                                    textView2.layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    textView2.textSize = 12f
                                    textView2.setTextColor(resources.getColor(R.color.black))
                                    textView2.setPadding(8, 8, 8, 8)
                                    textView2.text=s.getString("Artists")
                                    textView2.gravity = Gravity.CENTER
                                    linearLayout.addView(textView2)
                                }
                            }
                    }

                    numTexts=starredSongs.size
                    println(numTexts)
                }

            }
    }


    interface FollowersCountCallback {
        fun onSuccess(artists: String)
        fun onError(error: String)
    }

    private fun getFollowersCount(callback: FollowersCountCallback): String {
        var fno = ""

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .addHeader("Authorization", "Bearer ${Opening.token}")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("RRequest failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                println("RResponse data: ${responseData}")
                val jsonObject = JSONObject(responseData)
                val item = jsonObject.getJSONObject("followers")
                fno = item.getString("total")

                println("Item artists: ${fno}")
                callback.onSuccess(fno)
            }

        })
        println("Number of followers: ${fno}")
        return fno
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

    interface UsernameCallback {
        fun onSuccess(username: String)
        fun onError(error: String)
    }

    private fun getUsername(callback: UsernameCallback): String {
        var username = ""

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
                println("user data: ${responseData}")

                val jsonObject = JSONObject(responseData)
                username = jsonObject.getString("display_name")
                println("usernamee: $username")
//                println("Item artists: ${song}")
                callback.onSuccess(username)
            }

        })
        println("username2: $username")
        return username
    }

}
