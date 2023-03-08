package com.app.service.reviewaza.call

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.service.reviewaza.databinding.ItemCallBinding
import com.app.service.reviewaza.login.UserItem
import com.app.service.reviewaza.reviews.Reviews
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class CallListAdapter(private val onClick: (UserItem) -> Unit)
    : ListAdapter<UserItem, CallListAdapter.ViewHolder>(differ) {

    private var callList = mutableListOf<UserItem>()


    inner class ViewHolder(private val binding : ItemCallBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserItem) {
            binding.nicknameTextView.text = item.username
            binding.descriptionTextView.text = item.description

            if (item.userImage != null) {

                Glide.with(binding.callImageView)
                    .load(Uri.parse(item.userImage))
                    //.override(350, 350)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .circleCrop()
                    .into(binding.callImageView)
            }

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCallBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val differ = object :DiffUtil.ItemCallback<UserItem>() {
            override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
                return oldItem == newItem
            }

        }
    }

}