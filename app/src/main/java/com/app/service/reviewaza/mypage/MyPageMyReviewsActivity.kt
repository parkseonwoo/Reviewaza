package com.app.service.reviewaza.mypage

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.REVIEWS_DETAIL_FLAG
import com.app.service.reviewaza.databinding.ActivityLatestReviewsBinding
import com.app.service.reviewaza.reviews.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_reviews.*

class MyPageMyReviewsActivity : AppCompatActivity(), ReviewsAdapter.ItemClickListener {
    private lateinit var binding: ActivityLatestReviewsBinding
    private lateinit var reviewsAdapter: ReviewsAdapter
    private lateinit var reviews: Reviews
    private lateinit var searchAdapter: ReviewsSearchAdapter

    private val myUserId = Firebase.auth.currentUser?.uid ?: ""

    private val updateDeleteReviews = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isDelete = result.data?.getBooleanExtra("isDelete", false) ?: false

        if (result.resultCode == RESULT_OK && isDelete) {
            updateDeleteReviews()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        REVIEWS_DETAIL_FLAG = "MyPage_DETAILS"
        super.onCreate(savedInstanceState)
        binding = ActivityLatestReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.reviewsWriteButton.isEnabled = false
        binding.reviewsWriteButton.isVisible = false
        binding.reviewTypeSpinner.isVisible = false

        initRecyclerView()

        binding.reviewsSearchView.setOnQueryTextListener(searchViewTextListener)

        binding.toolBar.apply {
            title = "나의 리뷰 목록"
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
            var myUserEmail = Firebase.auth.currentUser?.email
            val list = AppDatabase.getInstance(this)?.reviewsDao()?.getMyReviews(myUserEmail!!)

            if (list != null) {
                reviewsAdapter.list.addAll(list)
            }

            runOnUiThread {
                binding.notFoundView.isVisible = list?.isEmpty() ?: true
                reviewsAdapter.notifyDataSetChanged()
            }
        }.start()

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
            Toast.makeText(this@MyPageMyReviewsActivity, "호출중...", Toast.LENGTH_SHORT).show()
        }

        Intent(this@MyPageMyReviewsActivity, ReviewsDetailActivity::class.java).apply {
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