package com.app.service.reviewaza.reviews

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.app.service.reviewaza.databinding.ItemReviewsBinding
import kotlinx.android.synthetic.main.item_reviews.view.*

class ReviewsAdapter(
    val list: MutableList<Reviews>,
    private val itemClickListener: ItemClickListener? = null
) : RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ItemReviewsBinding.inflate(inflater, parent, false)
        return ReviewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {

        val reviews = list[position]
        holder.bind(reviews)

        holder.itemView.setOnClickListener {
            itemClickListener?.onClick(reviews)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ReviewsViewHolder(private val binding: ItemReviewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reviews: Reviews) {
            binding.apply {
                reviewsRatingBar.rating = reviews.rating
                reviewsTaxiTypeValue.text = reviews.taxiType
                reviewsTaxiNumberValue.text = reviews.taxiNumber
                reviewsDateValueTextView.text = reviews.currentTime
            }
        }
    }

    interface ItemClickListener {
        fun onClick(reviews: Reviews)
    }
}