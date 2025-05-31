package com.example.doggiedon.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.doggiedon.R
import com.example.doggiedon.adapter.BlogAdapter
import com.example.doggiedon.model.BlogItemModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class UserBlogsActivity : AppCompatActivity() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private val blogList = mutableListOf<BlogItemModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_blog)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshUserBlogs)
        recyclerView = findViewById(R.id.userBlogRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        swipeRefreshLayout.setOnRefreshListener { fetchUserBlogs() }

        fetchUserBlogs()
    }

    private fun fetchUserBlogs() {
        swipeRefreshLayout.isRefreshing = true

        val uid = intent.getStringExtra("uid")
        if (uid.isNullOrEmpty()) {
            Toast.makeText(this, "UID not available", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = false
            finish() // Exit safely
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

                recyclerView.adapter = BlogAdapter(blogList)
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this, "Failed to fetch blogs: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
