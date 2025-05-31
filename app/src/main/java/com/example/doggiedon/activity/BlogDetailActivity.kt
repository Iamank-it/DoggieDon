package com.example.doggiedon.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.doggiedon.R

class BlogDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog_detail)

        val textHeading = findViewById<TextView>(R.id.text_heading)
        val textAuthor = findViewById<TextView>(R.id.text_author)
        val textDate = findViewById<TextView>(R.id.text_date)
        val textPost = findViewById<TextView>(R.id.text_post)

        // Get blog data from intent extras
        val heading = intent.getStringExtra("heading") ?: "No Title"
        val author = intent.getStringExtra("username") ?: "Unknown"
        val date = intent.getStringExtra("date") ?: ""
        val post = intent.getStringExtra("post") ?: "No Content"

        // Set views
        textHeading.text = heading
        textAuthor.text = "by $author"
        textDate.text = date
        textPost.text = post

    }
}
