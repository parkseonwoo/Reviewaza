package com.app.service.reviewaza.reviews

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.app.service.reviewaza.databinding.ItemReviewsBinding

class ReviewsSearchAdapter(
    val list: MutableList<Reviews>
) : RecyclerView.Adapter<ReviewsSearchAdapter.ViewHolder>() , Filterable {

    // TODO 간헐적으로 리뷰 검색이 안 되는 경우 오류 해결

    var filteredReviews : MutableList<Reviews> = mutableListOf()
    var itemFilter = ItemFilter()

    init {
        filteredReviews.addAll(list)
    }

    inner class ViewHolder(private val binding: ItemReviewsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reviews: Reviews) {
            binding.apply {
                reviewsRatingBar.rating = reviews.rating!!
                reviewsTaxiTypeValue.text = reviews.taxiType
                reviewsTaxiNumberValue.text = reviews.taxiNumber
                reviewsDateValueTextView.text = reviews.currentTime
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsSearchAdapter.ViewHolder {
        return ViewHolder(
            ItemReviewsBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ReviewsSearchAdapter.ViewHolder, position: Int) {

        //val reviews = list[position]
        val reviews : Reviews = filteredReviews[position]

        holder.bind(reviews)

    }

    override fun getItemCount(): Int {
        return filteredReviews.size
    }

    // -- Filter
    override fun getFilter(): Filter {
        return itemFilter
    }

    inner class ItemFilter : Filter() {

        override fun performFiltering(charSequence: CharSequence): FilterResults {

            val filterString: String = charSequence.toString()
            val results = FilterResults()

            //검색이 필요없을 경우를 위해 원본 배열을 복제
            val filteredList: MutableList<Reviews> =  mutableListOf()
            //공백제외 아무런 값이 없을 경우 -> 원본 배열
            if (filterString.trim { it <= ' ' }.isEmpty()) {
                results.values = list
                results.count = list.size

                return results
                //공백제외 2글자 이하인 경우 -> 이름으로만 검색
            } else if (filterString.trim { it <= ' ' }.length <= 2) {

                for (review in list) {
                    if (review.taxiNumber?.contains(filterString) == true) {
                        filteredList.add(review)
                    }
                }
                //그 외의 경우(공백제외 2글자 초과)
            } else {
                for (review in list) {
                    if (review.taxiType?.contains(filterString) == true || review.detail?.contains(filterString) == true) {
                        filteredList.add(review)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size

            return results

        }

        override fun publishResults(constraint: CharSequence, filterResults: FilterResults) {
            filteredReviews.clear()
            filteredReviews.addAll(filterResults.values  as MutableList<Reviews>)
            notifyDataSetChanged()
        }
    }
}