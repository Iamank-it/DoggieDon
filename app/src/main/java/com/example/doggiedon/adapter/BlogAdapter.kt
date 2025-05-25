package com.example.doggiedon.adapter
import com.example.doggiedon.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doggiedon.Model.BlogItemModel
import com.example.doggiedon.databinding.BlogItemBinding

class BlogAdapter(private val items: List<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BLogViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater, parent, false)
        return BLogViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BLogViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BLogViewHolder(private val binding: BlogItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(model: BlogItemModel) {
            binding.cardHeading.text = model.heading
            binding.cardUsername.text = model.username
            binding.cardDate.text = model.date
            binding.cardPost.text = model.post
            binding.cardPfp.setImageResource(R.drawable.dogpfp)
            binding.cardLikecount.text = model.likecount.toString()
        }

    }
}