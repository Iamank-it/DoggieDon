package com.example.doggiedon.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.example.doggiedon.R
import com.example.doggiedon.register.ProfileInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.net.URL

class BlogDetailActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var fabLike: FloatingActionButton
    private lateinit var fabSave: FloatingActionButton
    private var isLiked = false
    private var isSaved = false

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var blogId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog_detail)

        val textHeading = findViewById<TextView>(R.id.text_heading)
        val textAuthor = findViewById<TextView>(R.id.text_author)
        val textDate = findViewById<TextView>(R.id.text_date)
        val textPost = findViewById<TextView>(R.id.text_post)
        val btnProfile = findViewById<ImageButton>(R.id.btn_profile)

        fabLike = findViewById(R.id.fab_like)
        fabSave = findViewById(R.id.fab_save)

        FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()?.let {
            scope.launch {
                loadImageFromUrl(it)?.let { bmp ->
                    btnProfile.setImageBitmap(getCircularBitmap(bmp))
                }
            }
        }

        // Get blog data
        val heading = intent.getStringExtra("heading") ?: "No Title"
        val author = intent.getStringExtra("username") ?: "Unknown"
        val date = intent.getStringExtra("date") ?: ""
        val post = intent.getStringExtra("post") ?: "No Content"
        blogId = intent.getStringExtra("blogId") ?: ""

        textHeading.text = heading
        textAuthor.text = getString(R.string.blog_detail_username, author)
        textDate.text = date
        textPost.text = post

        fetchBlogState()
        setupLikeButton()
        setupSaveButton()

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileInfo::class.java))
        }
    }

    private fun fetchBlogState() {
        val uid = currentUser?.uid ?: return

        firestore.collection("blogs").document(blogId).get()
            .addOnSuccessListener { doc ->
                val likedBy = doc.get("likedby") as? List<*> ?: listOf<Any>()
                val savedBy = doc.get("savedby") as? List<*> ?: listOf<Any>()

                isLiked = likedBy.contains(uid)
                isSaved = savedBy.contains(uid)

                fabLike.setImageResource(if (isLiked) R.drawable.redheart else R.drawable.heart)
                fabSave.setImageResource(if (isSaved) R.drawable.saved else R.drawable.save)
            }
    }

    private fun setupLikeButton() {
        fabLike.setOnClickListener {
            val uid = currentUser?.uid ?: return@setOnClickListener
            val blogRef = firestore.collection("blogs").document(blogId)

            if (isLiked) {
                blogRef.update(
                    "likedby", FieldValue.arrayRemove(uid),
                    "likecount", FieldValue.increment(-1)
                ).addOnSuccessListener {
                    isLiked = false
                    fabLike.setImageResource(R.drawable.heart)
                }
            } else {
                blogRef.update(
                    "likedby", FieldValue.arrayUnion(uid),
                    "likecount", FieldValue.increment(1)
                ).addOnSuccessListener {
                    isLiked = true
                    fabLike.setImageResource(R.drawable.redheart)
                }
            }
        }
    }

    private fun setupSaveButton() {
        fabSave.setOnClickListener {
            val uid = currentUser?.uid ?: return@setOnClickListener
            val blogRef = firestore.collection("blogs").document(blogId)

            if (isSaved) {
                blogRef.update("savedby", FieldValue.arrayRemove(uid))
                    .addOnSuccessListener {
                        isSaved = false
                        fabSave.setImageResource(R.drawable.save)
                    }
            } else {
                blogRef.update("savedby", FieldValue.arrayUnion(uid))
                    .addOnSuccessListener {
                        isSaved = true
                        fabSave.setImageResource(R.drawable.saved)
                    }
            }
        }
    }

    private suspend fun loadImageFromUrl(url: String): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            BitmapFactory.decodeStream(URL(url).openStream())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCircularBitmap(src: Bitmap): Bitmap {
        val size = minOf(src.width, src.height)
        val output = createBitmap(size, size)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        return output
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
