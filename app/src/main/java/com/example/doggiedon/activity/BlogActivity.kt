package com.example.doggiedon.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.doggiedon.R
import com.example.doggiedon.model.Blog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class BlogActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_blog)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        titleInput = findViewById(R.id.etTitle)
        descriptionInput = findViewById(R.id.etDescription)
        val addButton = findViewById<Button>(R.id.btnAdd)

        addButton.setOnClickListener {
            addBlog()
        }
    }

    private fun addBlog() {
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        val blog = hashMapOf(
            "title" to title,
            "description" to description,
            "authorId" to uid,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("blogs")
            .add(blog)
            .addOnSuccessListener {
                Toast.makeText(this, "Blog added", Toast.LENGTH_SHORT).show()
                titleInput.text.clear()
                descriptionInput.text.clear()
            }
    }
}