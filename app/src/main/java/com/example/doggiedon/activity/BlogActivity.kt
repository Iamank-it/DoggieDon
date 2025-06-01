package com.example.doggiedon.activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.example.doggiedon.R
import com.example.doggiedon.register.ProfileInfo
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class BlogActivity : AppCompatActivity() {

    private lateinit var etHeading: EditText
    private lateinit var etPost: EditText
    private lateinit var btnSubmit: Button
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog)

        //profile btn
        val btnProfile = findViewById<ImageButton>(R.id.btn_profile)

        FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()?.let {
            scope.launch {
                loadImageFromUrl(it)?.let { bmp ->
                    btnProfile.setImageBitmap(getCircularBitmap(bmp))
                }
            }
        }


        etHeading = findViewById(R.id.etHeading)
        etPost = findViewById(R.id.etPost)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            addBlogPost()
        }
    }

    private fun addBlogPost() {
        val heading = etHeading.text.toString().trim()
        val post = etPost.text.toString().trim()

        if (heading.isEmpty()) {
            etHeading.error = "Heading required"
            return
        }

        if (post.isEmpty()) {
            etPost.error = "Post content required"
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val username = currentUser?.displayName ?: "Anonymous"
        val uid = currentUser?.uid ?: ""

        val blogMap = hashMapOf(
            "heading" to heading,
            "username" to username,
            "uid" to uid,
            "date" to Timestamp.now(),
            "post" to post,
            "likecount" to 0
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("blogs")
            .add(blogMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Blog added successfully", Toast.LENGTH_SHORT).show()
                finish()  // Close AddBlogActivity and return to previous screen
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add blog: ${e.message}", Toast.LENGTH_SHORT).show()
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


