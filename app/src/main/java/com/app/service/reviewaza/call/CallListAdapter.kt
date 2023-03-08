package com.app.service.reviewaza.call

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.service.reviewaza.databinding.ItemCallBinding
import com.app.service.reviewaza.login.UserItem
import com.app.service.reviewaza.reviews.Reviews

class CallListAdapter(private val onClick: (UserItem) -> Unit)
    : ListAdapter<UserItem, CallListAdapter.ViewHolder>(differ) {

    private var callList = mutableListOf<UserItem>()

    fun setListData(data: MutableList<UserItem>) {
        callList = data
    }

    inner class ViewHolder(private val binding : ItemCallBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserItem) {
            binding.nicknameTextView.text = item.username
            binding.descriptionTextView.text = item.description

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