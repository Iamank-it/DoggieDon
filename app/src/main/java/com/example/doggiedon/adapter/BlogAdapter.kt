package com.example.doggiedon.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doggiedon.R
import com.example.doggiedon.activity.BlogDetailActivity
import com.example.doggiedon.databinding.BlogItemBinding
import com.example.doggiedon.model.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class BlogAdapter(private val items: MutableList<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val binding = BlogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: BlogItemModel) {
            val context = binding.root.context
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            // Set data
            binding.cardHeading.text = model.heading
            binding.cardUsername.text = model.username
            binding.cardDate.text = model.date
            binding.cardPost.text = model.post
            binding.cardLikecount.text = model.likecount.toString()
            binding.cardPfp.setImageResource(R.drawable.dogpfp)

            // Set initial like button state
            val isLiked = model.likedby.contains(currentUserId)
            binding.btnCardLike.setImageResource(
                if (isLiked) R.drawable.redheart else R.drawable.heart
            )

            // Read more
            binding.btnCardReadmore.setOnClickListener {
                val intent = Intent(context, BlogDetailActivity::class.java).apply {
                    putExtra("heading", model.heading)
                    putExtra("username", model.username)
                    putExtra("date", model.date)
                    putExtra("post", model.post)
                }
                context.startActivity(intent)
            }

            //save blog
            val isSaved = model.savedby.contains(currentUserId)
            binding.btnCardSave.setImageResource(
                if (isSaved) R.drawable.saved else R.drawable.save
            )

            binding.btnCardSave.setOnClickListener {
                currentUserId?.let { uid ->
                    val blogRef = FirebaseFirestore.getInstance()
                        .collection("blogs")
                        .document(model.blogId)

                    if (isSaved) {
                        blogRef.update("savedby", FieldValue.arrayRemove(uid))
                            .addOnSuccessListener {
                                model.savedby.remove(uid)
                                notifyItemChanged(adapterPosition)
                            }
                    } else {
                        blogRef.update("savedby", FieldValue.arrayUnion(uid))
                            .addOnSuccessListener {
                                model.savedby.add(uid)
                                notifyItemChanged(adapterPosition)
                            }
                    }
                }
            }

            // Like button click
            var isUpdating = false

            binding.btnCardLike.setOnClickListener {
                if (isUpdating) return@setOnClickListener // Prevent multiple taps

                currentUserId?.let { uid ->
                    isUpdating = true
                    val blogRef = FirebaseFirestore.getInstance()
                        .collection("blogs")
                        .document(model.blogId)

                    val currentlyLiked = model.likedby.contains(uid)

                    val updates = if (currentlyLiked) {
                        mapOf(
                            "likedby" to FieldValue.arrayRemove(uid),
                            "likecount" to model.likecount - 1
                        )
                    } else {
                        mapOf(
                            "likedby" to FieldValue.arrayUnion(uid),
                            "likecount" to model.likecount + 1
                        )
                    }

                    blogRef.update(updates).addOnSuccessListener {
                        if (currentlyLiked) {
                            model.likedby.remove(uid)
                            model.likecount -= 1
                        } else {
                            model.likedby.add(uid)
                            model.likecount += 1
                        }
                        notifyItemChanged(adapterPosition)
                        isUpdating = false // Reset flag
                    }.addOnFailureListener {
                        isUpdating = false // Ensure flag is reset on error too
                    }
                }
            }
        }
    }
}
