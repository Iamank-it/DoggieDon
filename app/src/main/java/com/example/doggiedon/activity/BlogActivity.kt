package com.example.doggiedon.activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doggiedon.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BlogActivity : AppCompatActivity() {

    private lateinit var etHeading: EditText
    private lateinit var etPost: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog)

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
}
