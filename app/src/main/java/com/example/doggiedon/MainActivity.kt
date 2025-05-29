package com.example.doggiedon

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doggiedon.adapter.BlogAdapter
import com.example.doggiedon.model.BlogItemModel
import com.example.doggiedon.register.ProfileInfo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.net.URL
import androidx.core.graphics.createBitmap

class MainActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.blogRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val data = listOf(
            BlogItemModel(
                heading = "The Day We Found Bruno",
                username = "New Blogger",
                date = "May 25, 2025",
                post = "It was a hot summer afternoon when I first saw him—curled up under a parked scooter near our apartment gate...",
                likecount = 20
            ),
            BlogItemModel(
                heading = "Bruno’s First Night",
                username = "Caretaker",
                date = "May 26, 2025",
                post = "We made a small bed from cardboard and some blankets to keep Bruno warm for the night...",
                likecount = 18
            ),
            BlogItemModel(
                heading = "The Day We Found Bruno",
                username = "New Blogger",
                date = "May 25, 2025",
                post = "It was a hot summer afternoon when I first saw him—curled up under a parked scooter near our apartment gate...",
                likecount = 20
            ),
            BlogItemModel(
                heading = "Bruno’s First Night",
                username = "Caretaker",
                date = "May 26, 2025",
                post = "We made a small bed from cardboard and some blankets to keep Bruno warm for the night...",
                likecount = 18
            ),
            BlogItemModel(
                heading = "The Day We Found Bruno",
                username = "New Blogger",
                date = "May 25, 2025",
                post = "It was a hot summer afternoon when I first saw him—curled up under a parked scooter near our apartment gate...",
                likecount = 20
            ),
            BlogItemModel(
                heading = "Bruno’s First Night The Day We Found Bruno The Day We Found Bruno",
                username = "Caretaker",
                date = "May 26, 2025",
                post = "We made a small bed from cardboard and some blankets to keep Bruno warm for the night...",
                likecount = 18
            )
        )

        recyclerView.adapter = BlogAdapter(data)

        val btnProfile = findViewById<ImageButton>(R.id.btn_profile)

        FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()?.let {
            scope.launch {
                loadImageFromUrl(it)?.let { bmp ->
                    btnProfile.setImageBitmap(getCircularBitmap(bmp))
                }
            }
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileInfo::class.java))
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
