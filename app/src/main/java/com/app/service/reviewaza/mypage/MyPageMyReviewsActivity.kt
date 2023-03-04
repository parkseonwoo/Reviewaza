package com.app.service.reviewaza.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.REVIEWS_DETAIL_FLAG
import com.app.service.reviewaza.call.Key
import com.app.service.reviewaza.databinding.FragmentReviewsBinding
import com.app.service.reviewaza.reviews.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyPageMyReviewsActivity : AppCompatActivity() {
    private lateinit var binding: FragmentReviewsBinding
    private lateinit var reviewsAdapter: ReviewListAdapter
    private lateinit var reviews: Reviews
    private lateinit var searchAdapter: ReviewsSearchAdapter

    private var myUsername = FirebaseAuth.getInstance().currentUser?.email

    private val startMyReviews = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isDelete = result.data?.getBooleanExtra("isDelete", false) ?: false
        val isUpdate = result.data?.getBooleanExtra("isUpdate", false) ?: false

        if (result.resultCode == RESULT_OK && isDelete || isUpdate) {
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

    }


    private fun initRecyclerView() {

        val reviewsDB = Firebase.database.reference.child(Key.DB_REVIEWS)

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

            reviewsDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val reviewsList = mutableListOf<Reviews>()

                    snapshot.children.forEach {
                        val review = it.getValue(Reviews::class.java)

                        if (review?.userEmail == myUsername) {
                            reviewsList.add(review!!)
                        }
                        Log.e("리뷰 페이지", "userEmail: ${review?.userEmail}, mtUsername: ${myUsername}")
                    }

                    reviewsAdapter.submitList(reviewsList)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

//        Thread {
//            var myUserEmail = Firebase.auth.currentUser?.email
//            val list = ReviewDatabase.getInstance(this)?.reviewsDao()?.getMyReviews(myUserEmail!!)
//
//            if (list != null) {
//                reviewsAdapter.list.addAll(list)
//            }
//
//            runOnUiThread {
//                binding.notFoundView.isVisible = list?.isEmpty() ?: true
//                Toast.makeText(this, "list: $list", Toast.LENGTH_SHORT).show()
//                reviewsAdapter.submitList(list)
//            }
//        }.start()

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