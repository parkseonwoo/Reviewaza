package com.app.service.reviewaza.reviews

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.R
import com.app.service.reviewaza.REVIEWS_DETAIL_FLAG
import com.app.service.reviewaza.databinding.ActivityLatestReviewsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_reviews.*

class LatestReviewsActivity : AppCompatActivity(), ReviewsAdapter.ItemClickListener {
    private lateinit var binding: ActivityLatestReviewsBinding
    private lateinit var reviewsAdapter: ReviewsAdapter
    private lateinit var reviews: Reviews
    private lateinit var searchAdapter: ReviewsSearchAdapter

    private val myUserId = Firebase.auth.currentUser?.uid ?: null

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
        REVIEWS_DETAIL_FLAG = "LATEST_DETAILS"
        super.onCreate(savedInstanceState)
        binding = ActivityLatestReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()

        // 검색 기능에서 어댑터 연결해주기
        binding.reviewsSearchView.setOnQueryTextListener(searchViewTextListener)


        binding.toolBar.apply {
            title = "리뷰 목록"
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

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
            if (myUserId == null) {
                Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("리뷰쓰기", "$myUserId")
                Intent(this, ReviewsWriteActivity::class.java).let {
                    updateAddReviewsWriteResult.launch(it)
                }
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

    }

    private fun initRatingRecyclerView(list: List<Reviews>) {
        reviewsAdapter.list.addAll(0, list)

        runOnUiThread {
            reviewsAdapter.notifyDataSetChanged()
            binding.reviewsRecyclerView.apply {
                adapter = reviewsAdapter
                layoutManager =
                    LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                val dividerItemDecoration =
                    DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
                addItemDecoration(dividerItemDecoration)
            }

        }
    }

    private fun initSearchRecyclerView() {

        searchAdapter = ReviewsSearchAdapter(mutableListOf(), this)

        Thread {
            val list = AppDatabase.getInstance(this)?.reviewsDao()?.getAll() ?: emptyList()
            searchAdapter.list.addAll(list)
            runOnUiThread {
                searchAdapter.notifyDataSetChanged()
                binding.reviewsRecyclerView.apply {
                    adapter = searchAdapter
                    layoutManager =
                        LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                    val dividerItemDecoration =
                        DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
                    addItemDecoration(dividerItemDecoration)
                }
            }
        }.start()

    }

    //SearchView 텍스트 입력시 이벤트
    var searchViewTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {

            //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            //텍스트 입력/수정시에 호출
            override fun onQueryTextChange(s: String): Boolean {
                initSearchRecyclerView()
                searchAdapter.filter.filter(s)
                return false
            }
        }

    private fun updateAddReviews() {
        Thread {
            AppDatabase.getInstance(this)?.reviewsDao()?.getLatesReviews()?.let { reviews ->
                reviewsAdapter.list.add(0, reviews)
                runOnUiThread {
                    reviewsAdapter.notifyDataSetChanged()
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
                }
            }
        }.start()
    }

    private fun updateDeleteReviews() {
        Thread {
            AppDatabase.getInstance(this)?.reviewsDao()?.delete(this.reviews)?.let { reviews ->
                reviewsAdapter.list.remove(this.reviews)
                runOnUiThread {
                    reviewsAdapter.notifyDataSetChanged()
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

        Intent(this@LatestReviewsActivity, ReviewsDetailActivity::class.java).apply {
            putExtra("reviews", reviews)
            updateDeleteReviews.launch(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

}