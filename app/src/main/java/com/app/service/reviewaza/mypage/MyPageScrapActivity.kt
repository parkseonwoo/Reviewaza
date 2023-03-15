package com.app.service.reviewaza.mypage

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.call.Key
import com.app.service.reviewaza.databinding.ActivityMypageScrapBinding
import com.app.service.reviewaza.reviews.ReviewDatabase
import com.app.service.reviewaza.reviews.ReviewListAdapter
import com.app.service.reviewaza.reviews.Reviews
import com.app.service.reviewaza.reviews.ReviewsDetailActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyPageScrapActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMypageScrapBinding

    private lateinit var reviewsAdapter: ReviewListAdapter
    private val reviewDB = Firebase.database.reference.child(Key.DB_REVIEWS)

    private val myPageViewModel by lazy { ViewModelProvider(this).get(MyPageViewModel::class.java) }

    private lateinit var db: ReviewDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageScrapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()

        observerData()

    }

    private fun observerData() {
        db = ReviewDatabase.getInstance(this)!!
        db.reviewsDao().getAll().observe(this, Observer {
            reviewsAdapter.submitList(it)
            binding.notFoundView.isVisible = it?.isEmpty() ?: true
        })
    }

    private fun initRecyclerView() {

        reviewsAdapter = ReviewListAdapter(mutableListOf()) {

            val intent = Intent(this, ReviewsDetailActivity::class.java)
            intent.putExtra("reviews", it)
            startActivity(intent)

            if (it.reviewId != "") {
                reviewDB.get().addOnSuccessListener {
                    if (it.value != "") {
                        val reviewId = it.getValue(Reviews::class.java)
                        val currentReviewId = reviewId?.reviewId
                        intent.putExtra(
                            ReviewsDetailActivity.EXTRA_REVIEW_ID,
                            currentReviewId
                        )
                    }
                }
            }
        }

        binding.myPageScrapRecyclerView.apply {
            adapter = reviewsAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val dividerItemDecoration =
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)

        }
    }

}