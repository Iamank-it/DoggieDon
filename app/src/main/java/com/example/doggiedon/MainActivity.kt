package com.example.doggiedon

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnProfile = findViewById<ImageButton>(R.id.btn_profile)

        // Load profile picture
        val user = FirebaseAuth.getInstance().currentUser
        val photoUrl = user?.photoUrl?.toString()

        photoUrl?.let {
            scope.launch {
                val bitmap = loadImageFromUrl(it)
                bitmap?.let { bmp ->
                    val circular = getCircularBitmap(bmp)
                    btnProfile.setImageBitmap(circular)
                }
            }
        }

        // Profile button click
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileInfo::class.java)
            startActivity(intent)
        }
    }

    private suspend fun loadImageFromUrl(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val stream = URL(url).openStream()
            BitmapFactory.decodeStream(stream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCircularBitmap(srcBitmap: Bitmap): Bitmap {
        val size = minOf(srcBitmap.width, srcBitmap.height)
        val output = createBitmap(size, size)

        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(srcBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        return output
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
