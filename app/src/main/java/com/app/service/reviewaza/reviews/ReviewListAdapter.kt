package com.app.service.reviewaza.reviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.service.reviewaza.databinding.ItemReviewsBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ReviewListAdapter(
    val list: MutableList<Reviews>,
    private val onClick: (Reviews) -> Unit
) : ListAdapter<Reviews, ReviewListAdapter.ViewHolder>(differ) {

    inner class ViewHolder(private val binding: ItemReviewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reviews: Reviews) {
            binding.apply {
                reviewsRatingBar.rating = reviews.rating!!
                reviewsTaxiTypeValue.text = reviews.taxiType
                reviewsTaxiNumberValue.text = reviews.taxiNumber
                reviewsDateValueTextView.text = reviews.currentTime

                binding.root.setOnClickListener {
                    onClick(reviews)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemReviewsBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val reviews = list[position]
//        holder.bind(reviews)
        holder.bind(currentList[position])
    }

    companion object {
        val differ = object : DiffUtil.ItemCallback<Reviews>() {
            override fun areItemsTheSame(oldItem: Reviews, newItem: Reviews): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: Reviews, newItem: Reviews): Boolean {
                return oldItem == newItem
            }

        }
    }

}