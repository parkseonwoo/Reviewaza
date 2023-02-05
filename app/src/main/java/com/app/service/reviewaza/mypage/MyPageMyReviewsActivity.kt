package com.app.service.reviewaza.mypage

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.LOGIN_VALUE
import com.app.service.reviewaza.databinding.ActivityLatestReviewsBinding
import com.app.service.reviewaza.reviews.*
import kotlinx.android.synthetic.main.item_reviews.*

class MyPageMyReviewsActivity : AppCompatActivity(), ReviewsAdapter.ItemClickListener {
    private lateinit var binding: ActivityLatestReviewsBinding
    private lateinit var reviewsAdapter: ReviewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.reviewsWriteButton.isEnabled = false
        binding.reviewsWriteButton.isVisible = false

        initRecyclerView()
    }

    private fun initRecyclerView() {
        reviewsAdapter = ReviewsAdapter(mutableListOf(), this)
        binding.reviewsRecyclerView.apply {
            adapter = reviewsAdapter
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            val dividerItemDecoration =
                DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }

        Thread {
            val list = AppDatabase.getInstance(this)?.reviewsDao()?.getAll() ?: emptyList()
            reviewsAdapter.list.addAll(list)
            runOnUiThread {
                reviewsAdapter.notifyDataSetChanged()
            }
        }.start()

    }

    override fun onClick(reviews: Reviews) {

        reviewsTaxiTelNumberTextView.setOnClickListener {
            with(Intent(Intent.ACTION_DIAL)) {
                startActivity(this)
            }
            Toast.makeText(this@MyPageMyReviewsActivity, "호출중...", Toast.LENGTH_SHORT).show()
        }

        val intent = Intent(this@MyPageMyReviewsActivity, ReviewsDetailActivity::class.java).apply {
            putExtra("reviews", reviews)
        }
        startActivity(intent)

    }
}