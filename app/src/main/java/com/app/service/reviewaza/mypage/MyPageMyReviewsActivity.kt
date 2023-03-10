package com.app.service.reviewaza.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.REVIEWS_DETAIL_FLAG
import com.app.service.reviewaza.databinding.FragmentReviewsBinding
import com.app.service.reviewaza.reviews.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MyPageMyReviewsActivity : AppCompatActivity() {
    private lateinit var binding: FragmentReviewsBinding
    private lateinit var reviewsAdapter: ReviewListAdapter
    private lateinit var searchAdapter: ReviewsSearchAdapter

    private val reviewListViewModel by lazy { ViewModelProvider(this).get(ReviewListViewModel::class.java) }

    private val startMyReviews = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isUpdate = result.data?.getBooleanExtra("isUpdate", false) ?: false

        if (result.resultCode == RESULT_OK && isUpdate) {
            initRecyclerView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        REVIEWS_DETAIL_FLAG = "MyPage_DETAILS"
        super.onCreate(savedInstanceState)
        binding = FragmentReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.reviewsWriteButton.isEnabled = false
        binding.reviewsWriteButton.isVisible = false
        binding.reviewTypeSpinner.isVisible = false

        initRecyclerView()

        //binding.reviewsSearchView.setOnQueryTextListener(searchViewTextListener)

        binding.toolBar.apply {
            title = "나의 리뷰 목록"
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        observerData()
    }

    fun observerData() {

        Log.e("리뷰 데이터 변경", "observerData : 시작")

        reviewListViewModel.fetchMyReviewData().observe(this, Observer {
            Log.e("리뷰 데이터 변경", "observerData : reviewListViewModel.fetchData().observe")
            binding.notFoundView.isVisible = it?.isEmpty() ?: true
            reviewsAdapter.submitList(it)
        })

    }


    private fun initRecyclerView() {

        binding.bottomNavigationView.isVisible = false

        reviewsAdapter = ReviewListAdapter(mutableListOf()) {

            val intent = Intent(this, ReviewsDetailActivity::class.java)
            Log.e("마이페이지 init: ", "${it}")
            intent.putExtra("reviews", it)
            startMyReviews.launch(intent)

        }

        binding.reviewsRecyclerView.apply {
            adapter = reviewsAdapter
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            val dividerItemDecoration =
                DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)

        }

    }


//    private fun initSearchRecyclerView() {
//
//        searchAdapter = ReviewsSearchAdapter(mutableListOf(), this)
//
//        Thread {
//            val list = AppDatabase.getInstance(this)?.reviewsDao()?.getAll() ?: emptyList()
//            searchAdapter.list.addAll(list)
//            runOnUiThread {
//                searchAdapter.notifyDataSetChanged()
//                binding.reviewsRecyclerView.apply {
//                    adapter = searchAdapter
//                    layoutManager =
//                        LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
//                    val dividerItemDecoration =
//                        DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
//                    addItemDecoration(dividerItemDecoration)
//                }
//            }
//        }.start()
//
//    }

    //SearchView 텍스트 입력시 이벤트
//    var searchViewTextListener: SearchView.OnQueryTextListener =
//        object : SearchView.OnQueryTextListener {
//
//            //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
//            override fun onQueryTextSubmit(s: String): Boolean {
//                return false
//            }
//
//            //텍스트 입력/수정시에 호출
//            override fun onQueryTextChange(s: String): Boolean {
//                initSearchRecyclerView()
//                searchAdapter.filter.filter(s)
//                return false
//            }
//        }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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