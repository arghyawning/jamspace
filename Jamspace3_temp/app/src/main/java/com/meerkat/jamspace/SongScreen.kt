package com.meerkat.jamspace

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.meerkat.jamspace.Opening.Companion.token
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import com.google.firebase.firestore.FieldValue
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


class SongScreen : AppCompatActivity() {
    companion object {
        var dp = ""
    }

    val db = Firebase.firestore
    val songsDb = db.collection("songs")
    val commentsDb = db.collection("comments")
    val usersDb = db.collection("users")

    private lateinit var commentList: LinearLayout
    private lateinit var commentInput: EditText
    var imagelink: String? = null
//    var pp=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_screen)

        println("my frien ${Opening.userid}")

//        if (Opening.token == "")
//            println("hi")

        usersDb.whereEqualTo("userId", Opening.userid)
            .get()
            .addOnSuccessListener { documents ->
                println("umm $documents")
                if (!documents.isEmpty) {
                    val refs = documents.first()
//                    val tok = refs.get("token") as String
//                    println("we here: $tok")
//                    Opening.token = tok

                    dp = refs.get("userImgUrl") as String
                }

            }

        println("login token here! -> ${Opening.token}")
        val imageView = findViewById<ImageView>(R.id.image_view)
        imageView.setImageResource(R.drawable.loading)

        val SongName = findViewById<TextView>(R.id.song_name)

        val SongBy = findViewById<TextView>(R.id.song_by)

        val profilePic = findViewById<ImageView>(R.id.goto_profile_pic)

        SongName.text = "Song"
        SongBy.text = "Artist(s)"
        profilePic.setImageResource(R.drawable.profile_image)
        imageView.setImageResource(R.drawable.loading)

        if (Opening.token != "") {
            println("herehere $Opening.token")

            getCurrentlyPlaying(object : TrackIdCallback {
                override fun onSuccess(id: String) {
                    println("yayy $id")

                    songsDb.whereEqualTo("trackId", id)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                val createSong = hashMapOf<String, Any>(
                                    "trackId" to id,
                                    "starredUsers" to emptyList<String>()
                                )

                                val createsong = songsDb.add(createSong)
                                createsong.addOnSuccessListener { documentReference ->
                                    Log.d(
                                        "jam123",
                                        "DocumentSnapshot added with ID: ${documentReference.id}"
                                    )
                                }
                                createsong.addOnFailureListener { e ->
                                    Log.w("jam123", "Error adding document", e)
                                }

                            }

                        }

                }

                override fun onError(error: String) {
                    println("$error")
                }
            })

            getAlbumArt(object : AlbumArtCallback {
                override fun onSuccess(imgurl: String) {
                    runOnUiThread {
                        Glide.with(this@SongScreen)
                            .load(imgurl)
                            .into(imageView)
                    }

                    getCurrentlyPlaying(object : TrackIdCallback {
                        override fun onSuccess(id: String) {
                            println("inalbumid  $id")
//                        recreate()

                            songsDb.whereEqualTo("trackId", id)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    for (document in querySnapshot.documents) {
                                        // Handle each document here
                                        val documentId = document.id
                                        println("document album id  $document.id")

                                        val updateData = hashMapOf<String, Any>(
                                            "albumArtUrl" to imgurl
                                        )

                                        songsDb.document(documentId).update(updateData)
                                            .addOnSuccessListener {
                                                Log.d(
                                                    "TAG",
                                                    "Document updated successfully with art."
                                                )
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("TAG", "Error updating document.", e)
                                            }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    // Handle any errors here
                                }
                        }

                        override fun onError(error: String) {
                            println("$error")
                        }
                    })


//
                }

                override fun onError(error: String) {
                    println("$error")
                }
            })


            println("out12345")
            println("the image link: $imagelink")



            getSong(object : SongCallback {
                override fun onSuccess(song: String) {
                    println("yayy $song")
                    runOnUiThread {
                        SongName.text = song
                    }
                    getCurrentlyPlaying(object : TrackIdCallback {
                        override fun onSuccess(id: String) {
                            println("insongid  $id")

                            songsDb.whereEqualTo("trackId", id)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (querySnapshot.isEmpty) {
                                        println("empty")
                                    }
                                    for (document in querySnapshot.documents) {
                                        // Handle each document here
                                        val documentId = document.id
                                        println("document album id  $document.id")

                                        val updateData = hashMapOf<String, Any>(
                                            "songName" to song
                                        )

                                        songsDb.document(documentId).update(updateData)
                                            .addOnSuccessListener {
                                                Log.d(
                                                    "TAG",
                                                    "Document updated successfully with songname."
                                                )
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("TAG", "Error updating document.", e)
                                            }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    // Handle any errors here
                                }
                        }

                        override fun onError(error: String) {
                            println("$error")
                        }
                    })

                }

                override fun onError(error: String) {
                    println("$error")
                }
            })

//        SongBy.text = "Radiohead"
            getArtist(object : ArtistCallback {
                override fun onSuccess(artists: String) {
                    println("yayy1 $artists")
                    runOnUiThread {
                        SongBy.text = artists

                    }

                    getCurrentlyPlaying(object : TrackIdCallback {
                        override fun onSuccess(id: String) {
                            println("inalbumid  $id")

                            songsDb.whereEqualTo("trackId", id)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    for (document in querySnapshot.documents) {
                                        // Handle each document here
                                        val documentId = document.id
                                        println("document album id  $document.id")

                                        val updateData = hashMapOf<String, Any>(
                                            "Artists" to artists
                                        )

                                        songsDb.document(documentId).update(updateData)
                                            .addOnSuccessListener {
                                                Log.d(
                                                    "TAG",
                                                    "Document updated successfully with artists."
                                                )
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("TAG", "Error updating document.", e)
                                            }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    // Handle any errors here
                                }
                        }

                        override fun onError(error: String) {
                            println("$error")
                        }
                    })

                }

                override fun onError(error: String) {
                    println("$error")
                }
            })

            profilePic.setImageResource(R.drawable.profile_image)
            getUserPhotoArt(object : UserPhotoCallback {
                override fun onSuccess(imgUrl: String) {
                    println("omg ${imgUrl}")
                    runOnUiThread {
                        Glide.with(this@SongScreen)
                            .load(imgUrl)
                            .into(profilePic)
                    }

                }

                override fun onError(error: String) {
                    println("$error")
                }
            })
            profilePic.setOnClickListener { openProfilePage() }

            //bbbbbbbbbbbbbbbb
            getCurrentlyPlaying(object : TrackIdCallback {
                override fun onSuccess(id: String) {
                    songsDb.whereEqualTo("trackId", id)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val refs = documents.first()
                                val starredUsers = refs.get("starredUsers") as List<String>
                                if (starredUsers.contains(Opening.userid)) {
                                    val view = findViewById<View>(R.id.star)
                                    onStarClick(view)
                                }


                            }
                        }
                }

                override fun onError(error: String) {
                    println("$error")
                }
            })


            val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)

            val numImages = 10 // Set the number of images here

            //anu code
            getCurrentlyPlaying(object : TrackIdCallback {
                override fun onSuccess(id: String) {
//                    var starredUsers: List<String> = emptyList()
                    songsDb.whereEqualTo("trackId", id)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val refs = documents.first()
                                val starredUsers = refs.get("starredUsers") as List<String>
                                for (u in starredUsers) {
                                    val layout = LinearLayout(this@SongScreen)
                                    layout.layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    layout.orientation = LinearLayout.VERTICAL

                                    val frameLayout = FrameLayout(this@SongScreen)
                                    frameLayout.layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                    )

                                    val imageView = ImageView(this@SongScreen)
                                    imageView.layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                    )

//                                    println("plsplspls ${Opening.dp}")
                                    imageView.setPadding(8, 8, 8, 8)
//                                    imageView.setImageResource(
//                                        resources.getIdentifier(
//                                            "img2",
//                                            "drawable",
//                                            packageName
//                                        )
//                                    )


                                    //aaaaaaaaaaaaaaaaaaa
                                    var getuserr = ""
                                    usersDb.whereEqualTo("userId", u)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            println("umm $documents")
                                            if (!documents.isEmpty) {
                                                println("we in")
                                                val refs = documents.first()
                                                getuserr = refs.get("userImgUrl") as String
                                                Picasso.get().load(getuserr).into(imageView);
                                                println(getuserr)
                                            }

                                        }
                                    println("we out")

//                                    runOnUiThread {
//                                        Glide.with(this@SongScreen)
//                                            .load(Opening.dp)
//                                            .into(imageView)
//                                    }

                                    val heartView = ImageView(this@SongScreen)
                                    heartView.layoutParams = FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.WRAP_CONTENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT
                                        resources.getDimensionPixelSize(R.dimen.heart_size),
                                        resources.getDimensionPixelSize(R.dimen.heart_size)
                                    )
                                    heartView.setImageResource(R.drawable.heart)
                                    val params = heartView.layoutParams as FrameLayout.LayoutParams
                                    params.gravity = Gravity.BOTTOM or Gravity.LEFT
                                    params.marginStart = 8
                                    params.bottomMargin = 8

                                    val textView = TextView(this@SongScreen)
                                    textView.layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    textView.setPadding(8, 0, 8, 8)
                                    textView.text =
                                        "0 likes" // Replace 0 with the actual number of likes for the image

                                    frameLayout.addView(imageView)
                                    frameLayout.addView(heartView)
                                    layout.addView(frameLayout)
                                    layout.addView(textView)

                                    linearLayout.addView(layout)
                                }
                            }
                        }
                }

                override fun onError(error: String) {
                    println("$error")
                }
            })


//            for (i in 1..numImages) {
//                val layout = LinearLayout(this)
//                layout.layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//                layout.orientation = LinearLayout.VERTICAL
//
//                val frameLayout = FrameLayout(this)
//                frameLayout.layoutParams = FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.WRAP_CONTENT
//                )
//
//                val imageView = ImageView(this)
//                imageView.layoutParams = FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.WRAP_CONTENT
//                )
//                imageView.setPadding(8, 8, 8, 8)
//                imageView.setImageResource(
//                    resources.getIdentifier(
//                        "img$i",
//                        "drawable",
//                        packageName
//                    )
//                )
//
//                val heartView = ImageView(this)
//                heartView.layoutParams = FrameLayout.LayoutParams(
////                FrameLayout.LayoutParams.WRAP_CONTENT,
////                FrameLayout.LayoutParams.WRAP_CONTENT
//                    resources.getDimensionPixelSize(R.dimen.heart_size),
//                    resources.getDimensionPixelSize(R.dimen.heart_size)
//                )
//                heartView.setImageResource(R.drawable.heart)
//                val params = heartView.layoutParams as FrameLayout.LayoutParams
//                params.gravity = Gravity.BOTTOM or Gravity.LEFT
//                params.marginStart = 8
//                params.bottomMargin = 8
//
//                val textView = TextView(this)
//                textView.layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//                textView.setPadding(8, 0, 8, 8)
//                textView.text = "0 likes" // Replace 0 with the actual number of likes for the image
//
//                frameLayout.addView(imageView)
//                frameLayout.addView(heartView)
//                layout.addView(frameLayout)
//                layout.addView(textView)
//
//                linearLayout.addView(layout)
//            }


//            val LikesReblogs = findViewById<TextView>(R.id.likes_reblogs)
//            LikesReblogs.text = "10 likes and 4 reblogs"

            // Find comment UI components
            commentList = findViewById(R.id.comment_list)
            commentInput = findViewById(R.id.comment_input)

            // Add comment button click listener
            val commentButton = findViewById<Button>(R.id.comment_button)
            commentButton.setOnClickListener { addComment() }

            addCommentView("comment")

        }

    }

    private fun addComment() {
        val comment = commentInput.text.toString().trim()
        var user = ""
        println("here in add")

        if (comment.isNotEmpty()) {
            // Add comment to the UI

            getUsername(object : UsernameCallback {
                override fun onSuccess(username: String) {
                    println("in username ${username}")
                    user = username
                }

                override fun onError(error: String) {
                    println("$error")
                }
            })
            getCurrentlyPlaying(object : TrackIdCallback {
                override fun onSuccess(id: String) {
                    println("yayy $id")

                    songsDb.whereEqualTo("trackId", id)
                        .get()
                        .addOnSuccessListener { documents ->

                            if (!documents.isEmpty) {
                                val refs = documents.first()
                                val track = songsDb.document(refs.id)

                                val comm = hashMapOf<String, Any>(
                                    "trackId" to id,
                                    "commentBody" to comment,
                                    "userId" to Opening.userid,
                                    "username" to user
                                )

                                commentsDb.add(comm)
                                    .addOnSuccessListener { documentReference ->
                                        Log.d(
                                            "jam123comm",
                                            "DocumentSnapshot added with ID: ${documentReference.id}"
                                        )

                                        addCommentView(comment)

//                                        latch.countDown()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("jam123comm", "Error adding document", e)
//                                        latch.countDown()
                                    }


                            }

                        }

                }

                override fun onError(error: String) {
                    println("$error")
                }
            })
            // Clear the input field
            commentInput.text.clear()
        }
    }

    private fun addCommentView(comment: String) {
        // Create a new comment view
//        val commentView = TextView(this)

//        commentLayout.removeAllViews()
        commentList.removeAllViews()

        getCurrentlyPlaying(object : TrackIdCallback {
            override fun onSuccess(id: String) {
                println("yayy $id")

                commentsDb.whereEqualTo("trackId", id)
                    .get()
                    .addOnSuccessListener { documents ->

                        if (!documents.isEmpty) {
                            for (document in documents) {
                                println("hello comments $document.data")
//                                addCommentView(document)
                                val commentLayout = LinearLayout(this@SongScreen)
                                commentLayout.orientation = LinearLayout.HORIZONTAL
                                commentLayout.gravity = Gravity.CENTER_VERTICAL

                                val usernameView = TextView(this@SongScreen)
                                usernameView.text = document.getString("username")
                                usernameView.textSize = 16f
                                usernameView.setPadding(16, 0, 0, 0)
                                usernameView.setTypeface(
                                    null,
                                    Typeface.BOLD
                                ) // Add this line to make the text bold
                                usernameView.setTextColor(Color.BLACK) // Add this line to make the text black
                                commentLayout.addView(usernameView)

                                val commentView = TextView(this@SongScreen)

                                commentView.text = document.getString("commentBody")
                                commentView.textSize = 16f
                                commentView.setPadding(0, 16, 0, 16)
                                commentLayout.addView(commentView)

                                // Add the comment view to the comment list
                                commentList.addView(commentLayout)

                            }

                        }

                    }

            }

            override fun onError(error: String) {
                println("$error")
            }
        })
//
    }

    private fun openProfilePage() {
        val intent = Intent(this, UserProfile::class.java)
        startActivity(intent)
    }


    fun onStarClicked(view: View) {
        val star = view as ImageView
        if (star.drawable.constantState === resources.getDrawable(R.drawable.star_empty).constantState) {
            star.setImageResource(R.drawable.star_yellow)

            getCurrentlyPlaying(object : TrackIdCallback {
                override fun onSuccess(id: String) {
                    println("yayy $id")

                    songsDb.whereEqualTo("trackId", id)
                        .get()
                        .addOnSuccessListener { documents ->

                            if (!documents.isEmpty) {
                                val refs = documents.first()
                                val track = songsDb.document(refs.id)
                                track.update("starredUsers", FieldValue.arrayUnion(Opening.userid))

                            }

                        }
                    usersDb.whereEqualTo("userId", Opening.userid)
                        .get()
                        .addOnSuccessListener { documents ->

                            if (!documents.isEmpty) {
                                val refs = documents.first()
                                val user = usersDb.document(refs.id)
                                user.update("starredSongs", FieldValue.arrayUnion(id))

                            }

                        }
                }

                override fun onError(error: String) {
                    println("$error")
                }
            })


        } else {
            star.setImageResource(R.drawable.star_empty)

            getCurrentlyPlaying(object : TrackIdCallback {
                override fun onSuccess(id: String) {
                    println("yayy $id")

                    songsDb.whereEqualTo("trackId", id)
                        .get()
                        .addOnSuccessListener { documents ->

                            if (!documents.isEmpty) {
                                val refs = documents.first()
                                val track = songsDb.document(refs.id)
                                track.update("starredUsers", FieldValue.arrayRemove(Opening.userid))
                            }

                        }

                    usersDb.whereEqualTo("userId", Opening.userid)
                        .get()
                        .addOnSuccessListener { documents ->

                            if (!documents.isEmpty) {
                                val refs = documents.first()
                                val user = usersDb.document(refs.id)
                                user.update("starredSongs", FieldValue.arrayRemove(id))
                            }

                        }
                }

                override fun onError(error: String) {
                    println("$error")
                }
            })
        }
    }

    fun onStarClick(view: View) {
        val star = view as ImageView
        if (star.drawable.constantState === resources.getDrawable(R.drawable.star_empty).constantState) {
            star.setImageResource(R.drawable.star_yellow)

        }
    }

    //api calls

    private fun getCurrentlyPlaying() {
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/player/currently-playing")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                println("Response data: ${responseData}")
            }
        })
    }//test

    interface SongCallback {
        fun onSuccess(artists: String)
        fun onError(error: String)
    }

    private fun getSong(callback: SongCallback): String {
        var song = ""

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/player/currently-playing")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("RRequest failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
//                println("RResponse data: ${responseData}")
                val jsonObject = JSONObject(responseData)
                val item = jsonObject.getJSONObject("item")
//                val albumjson = item.getJSONObject("album")
                song = item.getString("name")

                println("Item artists: ${song}")
                callback.onSuccess(song)
            }

        })
//        callback.onSuccess(artists)
        println("The song: ${song}")
        return song
    }

    interface ArtistCallback {
        fun onSuccess(artists: String)
        fun onError(error: String)
    }

    private fun getArtist(callback: ArtistCallback): String {
        var artists = ""

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/player/currently-playing")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("RRequest failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
//                println("RResponse data: ${responseData}")
                val jsonObject = JSONObject(responseData)
                val item = jsonObject.getJSONObject("item")
                val artistsArray = item.getJSONArray("artists")

                //iterate through artistsArray
                println(artistsArray.length())
                for (i in 0 until artistsArray.length()) {
                    val artistjson = JSONObject(artistsArray[i].toString())
                    val name = artistjson["name"].toString()
                    println("${i}: ${name}")
                    if (i > 0)
                        artists = artists + ", "
                    artists = artists + name
                }

                println("Item artists: ${artists}")

                callback.onSuccess(artists)
            }

        })
//        callback.onSuccess(artists)
        println("The artists: ${artists}")
        return artists
    }

    interface AlbumArtCallback {
        fun onSuccess(imgUrl: String)
        fun onError(error: String)
    }

    private fun getAlbumArt(callback: AlbumArtCallback): String {
        var imgurl = ""

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/player/currently-playing")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("RRequest failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
//                println("RResponse data: ${responseData}")
                val jsonObject = JSONObject(responseData)
                val imgjson = jsonObject.getJSONObject("item").getJSONObject("album")
                    .getJSONArray("images")[0].toString()
                imgurl = JSONObject(imgjson)["url"].toString()
                println("Item: ${imgurl}")

                callback.onSuccess(imgurl) // Invoke the onSuccess callback with the imgUrl
            }

        })
        println(imgurl)
        return imgurl
    }

    interface UserPhotoCallback {
        fun onSuccess(imgUrl: String)
        fun onError(error: String)
    }

    private fun getUserPhotoArt(callback: UserPhotoCallback): String {
        var imgurl = ""

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/")
            .addHeader("Authorization", "Bearer $token")
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

    interface TrackIdCallback {
        fun onSuccess(artists: String)
        fun onError(error: String)
    }


    private fun getCurrentlyPlaying(callback: TrackIdCallback): String {
        var id = ""
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/player/currently-playing")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                println("Response data: ${responseData}")
                val jsonObject = JSONObject(responseData)
                val item = jsonObject.getJSONObject("item")
                id = item.getString("id")

                println("track iddd: ${id}")
                callback.onSuccess(id)
            }
        })
        println("The song id: ${id}")
        return id
    }


    interface UsernameCallback {
        fun onSuccess(username: String)
        fun onError(error: String)
    }

    private fun getUsername(callback: UsernameCallback): String {
        var username = ""

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/")
            .addHeader("Authorization", "Bearer $token")
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

//
//    private fun getStar() {
//
//        getCurrentlyPlaying(object : SongScreen.TrackIdCallback {
//            override fun onSuccess(id: String) {
//
//                usersDb.whereEqualTo("userId", Opening.userid)
//                    .get()
//                    .addOnSuccessListener { querySnapshot ->
//                        for (document in querySnapshot.documents) {
//                            val documentId = document.id
//
//                        }
//
//                    }
//                songsDb.whereEqualTo("trackId", id)
//                    .get()
//                    .addOnSuccessListener { querySnapshot ->
//                        for (document in querySnapshot.documents) {
//                            // Handle each document here
//                            val documentId = document.id
//                            println("document album id  $document.id")
//
//                            val song = document.get("starredSongs") as List<String>
//
//                            if (song.contains(id)) {
//                                star.setImageResource(R.drawable.star_yellow)
//                            } else {
//                                star.setImageResource(R.drawable.star_empty)
//                            }
//                        }
//                    }
//                    .addOnFailureListener { exception ->
//                        // Handle any errors here
//                    }
//            }
//
//            override fun onError(error: String) {
//                println("$error")
//            }
//        })
//    }

}