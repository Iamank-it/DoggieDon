package com.example.doggiedon

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.doggiedon.activity.BlogActivity
import com.example.doggiedon.activity.SavedBlogActivity
import com.example.doggiedon.adapter.BlogAdapter
import com.example.doggiedon.model.BlogItemModel
import com.example.doggiedon.register.ProfileInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val blogList = mutableListOf<BlogItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //Add Blog button click handle
        val fabAddBlog = findViewById<FloatingActionButton>(R.id.fab_add_blog)
        fabAddBlog.setOnClickListener {
            startActivity(Intent(this, BlogActivity::class.java))
        }
        //saved Blog click handle
        val fabSavedBlog = findViewById<FloatingActionButton>(R.id.fab_saved_blog)
        fabSavedBlog.setOnClickListener {
            startActivity(Intent(this, SavedBlogActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.blogRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        swipeRefreshLayout.setOnRefreshListener {
            fetchBlogsFromFirestore()
        }

        fetchBlogsFromFirestore()

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
        //search view
        val searchView = findViewById<SearchView>(R.id.search_bar)

        val searchButton = findViewById<ImageButton>(R.id.btn_search)

        searchButton.setOnClickListener {
            val query = searchView.query.toString().trim()
            if (query.isNotEmpty()) {
                val filteredList = blogList.filter {
                    it.heading.contains(query, ignoreCase = true) ||
                            it.post.contains(query, ignoreCase = true) ||
                            it.username.contains(query, ignoreCase = true)
                }
                recyclerView.adapter = BlogAdapter(filteredList.toMutableList())
            } else {
                recyclerView.adapter = BlogAdapter(blogList) // Reset to full list
            }
        }

        // Reset list when search is cleared
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    recyclerView.adapter = BlogAdapter(blogList)
                }
                return true
            }
        })

        //sort button logic
        val filterButton = findViewById<ImageButton>(R.id.btn_filter)

        filterButton.setOnClickListener {
            showSortOptions()
        }


    }

    private fun fetchBlogsFromFirestore() {
        swipeRefreshLayout.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch saved blog IDs first
        db.collection("users").document(currentUid)
            .collection("savedBlogs")
            .get()
            .addOnSuccessListener { savedResult ->
                val savedBlogIds = savedResult.documents.map { it.id }.toSet()

                db.collection("blogs")
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
                                    isSaved = savedBlogIds.contains(blogId),
                                    savedby = savedby
                                )
                            )
                        }

                        recyclerView.adapter = BlogAdapter(blogList)
                        swipeRefreshLayout.isRefreshing = false
                    }
                    .addOnFailureListener {
                        swipeRefreshLayout.isRefreshing = false
                        Toast.makeText(this, "Failed to fetch blogs", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this, "Failed to load saved data", Toast.LENGTH_SHORT).show()
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

    private fun showSortOptions() {
        val options = arrayOf("A-Z", "Z-A", "By Date", "By Like Count")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Sort Blogs By")
        builder.setItems(options) { _, which ->
            val sortedList = when (which) {
                0 -> blogList.sortedBy { it.heading.lowercase(Locale.ROOT) } // A-Z
                1 -> blogList.sortedByDescending { it.heading.lowercase(Locale.ROOT) } // Z-A
                2 -> blogList.sortedByDescending { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(it.date) } // Newest first
                3 -> blogList.sortedByDescending { it.likecount } // Most liked
                else -> blogList
            }
            recyclerView.adapter = BlogAdapter(sortedList.toMutableList())
        }
        builder.show()
    }

}


