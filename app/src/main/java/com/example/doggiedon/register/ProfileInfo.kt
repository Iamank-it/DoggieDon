package com.example.doggiedon.register

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.doggiedon.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.createBitmap
import com.example.doggiedon.activity.UserBlogActivity
import com.example.doggiedon.activity.WebViewerActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ProfileInfo : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val signOutBtn = findViewById<ImageView>(R.id.btn_google_signOut_button)
        val profilePicBtn = findViewById<ImageButton>(R.id.btn_ProfilePic)

        val emailText = findViewById<TextView>(R.id.text_email)
        val nameText = findViewById<TextView>(R.id.text_name)
        val phoneText = findViewById<TextView>(R.id.text_phone)
        val metaText = findViewById<TextView>(R.id.text_metadata)

        val viewBlogBtn = findViewById<Button>(R.id.btn_view_blog)

        //to open our website
        val websiteFab = findViewById<FloatingActionButton>(R.id.fab_open_website)
        websiteFab.setOnClickListener {
            val intent = Intent(this, WebViewerActivity::class.java)
            intent.putExtra("url", "https://doggiedon-landing.vercel.app/")
            startActivity(intent)
        }


        // Set user info
        user?.let {
            emailText.text = it.email ?: "No Email"
            nameText.text = getString(R.string.user_name, it.displayName ?: "N/A")
            phoneText.text = getString(R.string.user_phone, it.phoneNumber ?: "N/A")

            val metadata = it.metadata
            val created = metadata?.creationTimestamp?.let { ts ->
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(ts))
            } ?: "N/A"
            val lastSignIn = metadata?.lastSignInTimestamp?.let { ts ->
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(ts))
            } ?: "N/A"
            metaText.text = getString(R.string.account_metadata, created, lastSignIn)


            // Set profile picture when clicking the button
            it.photoUrl?.let { photoUrl ->
                loadProfileImage(photoUrl.toString(), profilePicBtn)
            }
        }

        //number of blog
        val user1 = FirebaseAuth.getInstance().currentUser
        if (user1 != null) {
            fetchBlogCountForUser(user1.uid)
        }


        //view blog button click handle
        viewBlogBtn.setOnClickListener(View.OnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val intent = Intent(this, UserBlogActivity::class.java)
                intent.putExtra("uid", user.uid)  // Send UID explicitly
                startActivity(intent)
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        })

        // Sign out confirmation
        signOutBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch {
                        GoogleAuthClient(this@ProfileInfo).signOut()
                        startActivity(Intent(this@ProfileInfo, Welcome::class.java))
                        finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Zoom-in dialog on profile picture button tap
        profilePicBtn.setOnClickListener {
            val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
            dialog.setContentView(R.layout.dialog_enlarged_image)

            val imageView = dialog.findViewById<ImageView>(R.id.enlarged_image_view)
            val closeBtn = dialog.findViewById<ImageView>(R.id.btn_close)
            val root = dialog.findViewById<FrameLayout>(R.id.dialog_root)

            // Load profile picture into the dialog (same as main profile image)
            user?.photoUrl?.let { photoUrl ->
                loadProfileImage(photoUrl.toString(), imageView)
            }

            // Animate zoom-in
            imageView.scaleX = 0.7f
            imageView.scaleY = 0.7f
            imageView.alpha = 0f
            imageView.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(300).start()

            val dismissZoom = {
                imageView.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction { dialog.dismiss() }
                    .start()
            }

            closeBtn.setOnClickListener { dismissZoom() }
            root.setOnClickListener { dismissZoom() }

            dialog.show()
        }
    }

    // Function to load image using coroutines
    private fun loadProfileImage(url: String, imageView: ImageView) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                loadImageFromUrl(url)
            }
            bitmap?.let {
                val circularBitmap = getCircularBitmap(it)
                imageView.setImageBitmap(circularBitmap)
            }
        }
    }


    //Function to count number of blogs
    private fun fetchBlogCountForUser(uid: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("blogs")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                val count = result.size()
                val blogCountTextView = findViewById<TextView>(R.id.text_blog_count)
                blogCountTextView.text = getString(R.string.blog_count_display, count)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load blog count", Toast.LENGTH_SHORT).show()
            }
    }


    // Function to download image from URL
    private fun loadImageFromUrl(urlString: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }
}
private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
    val size = minOf(bitmap.width, bitmap.height)
    val output = createBitmap(size, size)

    val canvas = android.graphics.Canvas(output)
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
    }

    val rect = android.graphics.Rect(0, 0, size, size)
    val rectF = android.graphics.RectF(rect)

    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawOval(rectF, paint)

    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, null, rect, paint)

    return output
}
