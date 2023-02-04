package com.app.service.reviewaza.reviews

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.R
import com.app.service.reviewaza.databinding.ActivityLatestReviewsBinding
import com.app.service.reviewaza.databinding.ItemReviewsBinding

class LatestReviewsActivity : AppCompatActivity(), ReviewsAdapter.ItemClickListener {
    private lateinit var binding: ActivityLatestReviewsBinding
    private lateinit var reviewsAdapter: ReviewsAdapter

    private val updateAddReviewsWriteResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isUpdated = result.data?.getBooleanExtra("isUpdated", false) ?: false
        updateAddReviews()

        if (result.resultCode == RESULT_OK && isUpdated) {
            updateAddReviews()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()

        var spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.review_types,
            android.R.layout.simple_list_item_1
        )

        binding.reviewTypeSpinner.adapter = spinnerAdapter

        binding.reviewTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> Thread {

                        val list = AppDatabase.getInstance(this@LatestReviewsActivity)?.reviewsDao()?.getAll() ?: emptyList()
                        reviewsAdapter.list.addAll(0, list)
                        runOnUiThread {
                            Toast.makeText(this@LatestReviewsActivity, "$position", Toast.LENGTH_SHORT).show()
                            reviewsAdapter.notifyDataSetChanged() }
                    }.start()
                    1 -> Thread {

                        val list = AppDatabase.getInstance(this@LatestReviewsActivity)?.reviewsDao()?.getHigerReviews() ?: emptyList()
                        reviewsAdapter.list.addAll(list)
                        runOnUiThread {
                            Toast.makeText(this@LatestReviewsActivity, "$position", Toast.LENGTH_SHORT).show()
                            reviewsAdapter.notifyDataSetChanged() }
                    }.start()
                    else -> Thread {

                        val list = AppDatabase.getInstance(this@LatestReviewsActivity)?.reviewsDao()?.getLowerReviews() ?: emptyList()
                        reviewsAdapter.list.addAll(list)
                        runOnUiThread {
                            Toast.makeText(this@LatestReviewsActivity, "$position", Toast.LENGTH_SHORT).show()
                            reviewsAdapter.notifyDataSetChanged() }
                    }.start()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(this@LatestReviewsActivity, "아무것도 선택되지 않음", Toast.LENGTH_SHORT).show()
            }

        }

        binding.reviewsWriteButton.setOnClickListener {
            Intent(this, ReviewsWriteActivity::class.java).let {
                updateAddReviewsWriteResult.launch(it)
            }
        }

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
        val a = ItemReviewsBinding.inflate(layoutInflater)

        Thread {
            val list = AppDatabase.getInstance(this)?.reviewsDao()?.getAll() ?: emptyList()
            reviewsAdapter.list.addAll(list)
            runOnUiThread {
                reviewsAdapter.notifyDataSetChanged()
            }
        }.start()

    }

    private fun updateAddReviews() {
        Thread {
            AppDatabase.getInstance(this)?.reviewsDao()?.getLatesReviews()?.let { reviews ->
                reviewsAdapter.list.add(0, reviews)
                runOnUiThread { reviewsAdapter.notifyDataSetChanged() }
            }
        }.start()
    }

    override fun onClick(reviews: Reviews) {

    }
}