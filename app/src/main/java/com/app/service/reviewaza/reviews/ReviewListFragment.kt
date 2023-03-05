package com.app.service.reviewaza.reviews

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.R
import com.app.service.reviewaza.REVIEWS_DETAIL_FLAG
import com.app.service.reviewaza.call.Key
import com.app.service.reviewaza.databinding.FragmentReviewsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ReviewListFragment : Fragment(R.layout.fragment_reviews) {

    private lateinit var binding: FragmentReviewsBinding
    private lateinit var reviewsAdapter: ReviewListAdapter
    private lateinit var searchAdapter: ReviewsSearchAdapter

    private val myUserId = Firebase.auth.currentUser?.uid ?: null

    private val reviewDB = Firebase.database.reference.child(Key.DB_REVIEWS)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReviewsBinding.bind(view)
        initRecyclerView()

        REVIEWS_DETAIL_FLAG = "REVIEW_LIST_DETAIL"

        val updateViewResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val isUpdated = result.data?.getBooleanExtra("isUpdated", false) ?: false

            if (result.resultCode == RESULT_OK && isUpdated) {
                initRecyclerView()
            }
        }

        // 검색 기능에서 어댑터 연결해주기
        binding.reviewsSearchView.setOnQueryTextListener(searchViewTextListener)

        binding.toolBar.apply {
            title = "리뷰 목록"
        }

        var spinnerAdapter = ArrayAdapter.createFromResource(
            context!!,
            R.array.review_types,
            android.R.layout.simple_list_item_1
        )

        binding.reviewTypeSpinner.adapter = spinnerAdapter

        binding.reviewTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {

                    0 -> { // 최신순
                       initRecyclerView()
                    }

                    1 -> { // 별점 높은 순
                        reviewDB.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                val reviewsList = mutableListOf<Reviews>()

                                snapshot.children.forEach {
                                    val review = it.getValue(Reviews::class.java)
                                    val reviewId = review?.reviewId

                                    if (review?.reviewId != "") {
                                        reviewsList.add(review!!)
                                    }

                                    val reviewQuery = reviewDB.child(reviewId!!).orderByChild("rating")

                                    reviewQuery.addListenerForSingleValueEvent(object: ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (review?.reviewId != "") {
                                                reviewsList.add(review!!)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                                }
                                Log.e("리뷰 별점 정렬", "review")
                                reviewsAdapter.submitList(reviewsList)
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }

                    2 -> { // 별점 낮은 순

                    }

                    //일치하는게 없는 경우
                    else -> {

                    }
                }
            }
        }

        binding.reviewsWriteButton.setOnClickListener {
            if (myUserId == null) {
                Toast.makeText(context, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("리뷰쓰기", "$myUserId")
                Intent(context, ReviewsWriteActivity::class.java).let {
                    updateViewResult.launch(it)
                }
            }
        }

    }

    private fun initRecyclerView() {

        reviewsAdapter = ReviewListAdapter(mutableListOf()) {

            val intent = Intent(context, ReviewsDetailActivity::class.java)
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

        binding.reviewsRecyclerView.apply {
            adapter = reviewsAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val dividerItemDecoration =
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)

            reviewDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val reviewsList = mutableListOf<Reviews>()

                    snapshot.children.forEach {
                        val review = it.getValue(Reviews::class.java)

                        if (review?.reviewId != "") {
                            reviewsList.add(review!!)
                        }
                        Log.e("리뷰 페이지", "reviewId: ${review.reviewId}")
                    }

                    reviewsList.reverse()
                    reviewsAdapter.submitList(reviewsList)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

//    private fun initRatingRecyclerView(list: List<Reviews>) {
//        reviewsAdapter.list.addAll(0, list)
//
//        runOnUiThread {
//            reviewsAdapter.notifyDataSetChanged()
//            binding.reviewsRecyclerView.apply {
//                adapter = reviewsAdapter
//                layoutManager =
//                    LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
//                val dividerItemDecoration =
//                    DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
//                addItemDecoration(dividerItemDecoration)
//            }
//
//        }
//    }

    private fun initSearchRecyclerView() {

        searchAdapter = ReviewsSearchAdapter(mutableListOf())
        //val list = ReviewDatabase.getInstance(context!!)?.reviewsDao()?.getAll() ?: emptyList()
        //searchAdapter.list.addAll(list)
        //searchAdapter.notifyDataSetChanged()
        binding.reviewsRecyclerView.apply {
            adapter = searchAdapter
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val dividerItemDecoration =
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                //finish()
                true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

}