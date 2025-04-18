package com.example.doggiedon.register

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.doggiedon.R
import kotlinx.coroutines.launch

class ProfileInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)

        val googleAuthClient = GoogleAuthClient(this)

        val btn1 = findViewById<Button>(R.id.btn_google_signOut_button)
        btn1.setOnClickListener {
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes") { _, _ ->
                    // Sign out and redirect to Welcome
                    lifecycleScope.launch {
                        googleAuthClient.signOut()
                        startActivity(Intent(this@ProfileInfo, Welcome::class.java))
                        finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val profileButton = findViewById<ImageButton>(R.id.btn_ProfilePic)

        profileButton.setOnClickListener {
            val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
            dialog.setContentView(R.layout.dialog_enlarged_image)

            val imageView = dialog.findViewById<ImageView>(R.id.enlarged_image_view)
            val closeBtn = dialog.findViewById<ImageView>(R.id.btn_close)
            val root = dialog.findViewById<FrameLayout>(R.id.dialog_root)

            imageView.setImageResource(R.drawable.dogpfp)
            // Start with scale 0
            imageView.scaleX = 0.7f
            imageView.scaleY = 0.7f
            imageView.alpha = 0f

            // Animate zoom-in
            imageView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .start()

            // Close actions
            val dismissWithZoomOut = {
                imageView.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction { dialog.dismiss() }
                    .start()
            }

            closeBtn.setOnClickListener { dismissWithZoomOut() }
            root.setOnClickListener { dismissWithZoomOut() }

            dialog.show()
        }
    }
}
