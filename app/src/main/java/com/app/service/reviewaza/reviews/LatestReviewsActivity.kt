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
import kotlinx.android.synthetic.main.item_reviews.*

class LatestReviewsActivity : AppCompatActivity(), ReviewsAdapter.ItemClickListener {
    private lateinit var binding: ActivityLatestReviewsBinding
    private lateinit var reviewsAdapter: ReviewsAdapter
    private lateinit var reviews: Reviews

    private val updateAddReviewsWriteResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isUpdated = result.data?.getBooleanExtra("isUpdated", false) ?: false

        if (result.resultCode == RESULT_OK && isUpdated) {
            updateAddReviews()
        }
    }

    private val updateDeleteReviews = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isDelete = result.data?.getBooleanExtra("isDelete", false) ?: false

        if (result.resultCode == RESULT_OK && isDelete) {
            updateDeleteReviews()
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

        binding.reviewTypeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    reviewsAdapter = ReviewsAdapter(mutableListOf(), this@LatestReviewsActivity)

                    when (position) {

                        0 -> Thread {
                            val list =
                                AppDatabase.getInstance(this@LatestReviewsActivity)?.reviewsDao()
                                    ?.getAll() ?: emptyList()
                            initRatingRecyclerView(list)

                        }.start()
                        1 -> Thread {
                            val list =
                                AppDatabase.getInstance(this@LatestReviewsActivity)?.reviewsDao()
                                    ?.getHigerReviews() ?: emptyList()
                            initRatingRecyclerView(list)
                        }.start()
                        else -> Thread {
                            val list =
                                AppDatabase.getInstance(this@LatestReviewsActivity)?.reviewsDao()
                                    ?.getLowerReviews() ?: emptyList()
                            initRatingRecyclerView(list)
                        }.start()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Toast.makeText(this@LatestReviewsActivity, "아무것도 선택되지 않음", Toast.LENGTH_SHORT)
                        .show()
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

        Thread {
            val list = AppDatabase.getInstance(this)?.reviewsDao()?.getAll() ?: emptyList()
            reviewsAdapter.list.addAll(list)
            runOnUiThread {
                reviewsAdapter.notifyDataSetChanged()
            }
        }.start()

    }

    private fun initRatingRecyclerView(list: List<Reviews>) {
        reviewsAdapter.list.addAll(0, list)

        runOnUiThread {
            binding.reviewsRecyclerView.apply {
                adapter = reviewsAdapter
                layoutManager =
                    LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                val dividerItemDecoration =
                    DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
                addItemDecoration(dividerItemDecoration)
            }

            reviewsAdapter.notifyDataSetChanged()
        }
    }

    private fun updateAddReviews() {
        Thread {
            AppDatabase.getInstance(this)?.reviewsDao()?.getLatesReviews()?.let { reviews ->
                reviewsAdapter.list.add(0, reviews)
                runOnUiThread {
                    binding.reviewsRecyclerView.apply {
                        adapter = reviewsAdapter
                        layoutManager =
                            LinearLayoutManager(
                                applicationContext,
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        val dividerItemDecoration =
                            DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
                        addItemDecoration(dividerItemDecoration)
                    }

                    reviewsAdapter.notifyDataSetChanged()
                }
            }
        }.start()
    }

    private fun updateDeleteReviews() {
        Thread {
            AppDatabase.getInstance(this)?.reviewsDao()?.delete(this.reviews)?.let { reviews ->
                reviewsAdapter.list.remove(this.reviews)
                runOnUiThread {
                    binding.reviewsRecyclerView.apply {
                        adapter = reviewsAdapter
                        layoutManager =
                            LinearLayoutManager(
                                applicationContext,
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        val dividerItemDecoration =
                            DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
                        addItemDecoration(dividerItemDecoration)
                    }

                    reviewsAdapter.notifyDataSetChanged()
                }
            }
        }.start()
    }

    override fun onClick(reviews: Reviews) {
        this.reviews = reviews
        reviewsTaxiTelNumberTextView.setOnClickListener {
            with(Intent(Intent.ACTION_DIAL)) {
                startActivity(this)
            }
            Toast.makeText(this@LatestReviewsActivity, "호출중...", Toast.LENGTH_SHORT).show()
        }

        val intent = Intent(this@LatestReviewsActivity, ReviewsDetailActivity::class.java).apply {
            putExtra("reviews", reviews)
            updateDeleteReviews.launch(this)
        }

    }
}