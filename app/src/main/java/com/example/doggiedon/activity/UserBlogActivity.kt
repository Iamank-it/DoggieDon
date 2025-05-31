package com.example.doggiedon.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.doggiedon.R
import com.example.doggiedon.adapter.BlogAdapter
import com.example.doggiedon.model.BlogItemModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class UserBlogActivity : AppCompatActivity() {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private val blogList = mutableListOf<BlogItemModel>()
    private lateinit var adapter: BlogAdapter
    private var uid: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_blog)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        uid = intent.getStringExtra("uid")
        Log.d("Firebase Uid", "UserBlogActivity")

        swipeRefreshLayout = findViewById(R.id.swipeRefreshUserBlogs)
        recyclerView = findViewById(R.id.userBlogRecyclerView)
        emptyTextView = findViewById(R.id.text_empty_blogs) // Add this to layout

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BlogAdapter(blogList)
        recyclerView.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener { fetchUserBlogs() }

        fetchUserBlogs()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchUserBlogs() {
        swipeRefreshLayout.isRefreshing = true

        if (uid.isNullOrEmpty()) {
            Toast.makeText(this, "UID not available", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = false
            finish()
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("blogs")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                blogList.clear()
                for (document in result) {
                    val heading = document.getString("heading") ?: ""
                    val username = document.getString("username") ?: ""
                    val post = document.getString("post") ?: ""
                    val likecount = document.getLong("likecount")?.toInt() ?: 0

                    val timestamp = document.getTimestamp("date")
                    val dateString = timestamp?.toDate()?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                    } ?: ""

                    blogList.add(
                        BlogItemModel(
                            heading = heading,
                            username = username,
                            date = dateString,
                            post = post,
                            likecount = likecount
                        )
                    )
                }

                adapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false

                // Show message if no blogs posted
                emptyTextView.visibility = if (blogList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this, "Failed to fetch blogs: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}