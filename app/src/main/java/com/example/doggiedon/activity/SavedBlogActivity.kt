package com.example.doggiedon.activity

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class SavedBlogActivity : AppCompatActivity() {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private val blogList = mutableListOf<BlogItemModel>()
    private lateinit var adapter: BlogAdapter
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved_blog)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshUserBlogs)
        recyclerView = findViewById(R.id.userBlogRecyclerView)
        emptyTextView = findViewById(R.id.text_empty_blogs)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BlogAdapter(blogList)
        recyclerView.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener { fetchSavedBlogs() }

        fetchSavedBlogs()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchSavedBlogs() {
        swipeRefreshLayout.isRefreshing = true

        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = false
            return
        }

        FirebaseFirestore.getInstance().collection("blogs")
            .whereArrayContains("savedby", currentUserId)
            .get()
            .addOnSuccessListener { result ->
                blogList.clear()
                for (document in result) {
                    val heading = document.getString("heading") ?: ""
                    val username = document.getString("username") ?: ""
                    val post = document.getString("post") ?: ""
                    val likecount = document.getLong("likecount")?.toInt() ?: 0
                    val savedby = document.get("savedby") as? MutableList<String> ?: mutableListOf()
                    val likedby = document.get("likedby") as? MutableList<String> ?: mutableListOf()
                    val blogId = document.id

                    val timestamp = document.getTimestamp("date")
                    val dateString = timestamp?.toDate()?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                    } ?: ""

                    blogList.add(
                        BlogItemModel(
                            blogId = blogId,
                            heading = heading,
                            username = username,
                            date = dateString,
                            post = post,
                            likecount = likecount,
                            likedby = likedby,
                            savedby = savedby
                        )
                    )
                }

                adapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false

                emptyTextView.visibility = if (blogList.isEmpty()) View.VISIBLE else View.GONE
                emptyTextView.text = getString(R.string.no_saved_blogs)
            }
            .addOnFailureListener {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this, "Failed to fetch saved blogs: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
