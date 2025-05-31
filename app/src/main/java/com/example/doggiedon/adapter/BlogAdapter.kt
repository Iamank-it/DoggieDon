package com.example.doggiedon.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doggiedon.R
import com.example.doggiedon.activity.BlogDetailActivity
import com.example.doggiedon.databinding.BlogItemBinding
import com.example.doggiedon.model.BlogItemModel

class BlogAdapter(private val items: List<BlogItemModel>) :
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
            binding.cardHeading.text = model.heading
            binding.cardUsername.text = model.username
            binding.cardDate.text = model.date
            binding.cardPost.text = model.post
            binding.cardLikecount.text = model.likecount.toString()
            binding.cardPfp.setImageResource(R.drawable.dogpfp) // static image

            binding.btnCardReadmore.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, BlogDetailActivity::class.java).apply {
                    putExtra("heading", model.heading)
                    putExtra("username", model.username)
                    putExtra("date", model.date)
                    putExtra("post", model.post)
                }
                context.startActivity(intent)
            }
        }
    }
}
