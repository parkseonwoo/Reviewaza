package com.app.service.reviewaza.reviews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.service.reviewaza.REVIEWS_DETAIL_FLAG
import com.app.service.reviewaza.call.CallActivity
import com.app.service.reviewaza.call.Key
import com.app.service.reviewaza.databinding.ActivityReviewsDetailBinding
import com.app.service.reviewaza.login.UserItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ReviewsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewsDetailBinding
    private lateinit var reviews: Reviews
    private lateinit var reviewsAdapter: ReviewListAdapter

    private val currentUserDB = Firebase.database.reference.child(Key.DB_USERS)
    private val currentReviewDB = Firebase.database.reference.child(Key.DB_REVIEWS)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityReviewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reviews = intent.getParcelableExtra("reviews")!!

        if (REVIEWS_DETAIL_FLAG != "MyPage_DETAILS") {
            binding.reviewsEditButton.isEnabled = false
            binding.reviewsDeleteButton.isEnabled = false
            binding.reviewsEditButton.isVisible = false
            binding.reviewsDeleteButton.isVisible = false
        } else {
            binding.reviewsEditButton.isEnabled = true
            binding.reviewsDeleteButton.isEnabled = true
            binding.reviewsEditButton.isVisible = true
            binding.reviewsDeleteButton.isVisible = true
        }

        binding.reviewsEditButton.setOnClickListener {
            Toast.makeText(this, "수정 버튼 클릭", Toast.LENGTH_SHORT).show()
            //updateReview()
        }

        binding.reviewsDeleteButton.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setMessage("정말 삭제하시겠습니까?")
                setNegativeButton("취소", null)
                setPositiveButton("확인") { _, _ ->
                    delete()
                }.show()
            }
        }

        initView()

    }

    private fun initView() {
        reviewsAdapter = ReviewListAdapter(mutableListOf()) {

        }
        binding.reviewsUserEmail.text = reviews.userEmail
        binding.reviewsWriteRatingBar.rating = reviews.rating!!
        binding.reviewsWriteTaxiTypeValueTextView.text = reviews.taxiType
        binding.reviewsWriteTaxiNumberValueTextView.text = reviews.taxiNumber
        binding.reviewsWriteDetailTextView.text = reviews.detail

        currentUserDB.addListenerForSingleValueEvent(object :
            ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEach {
                    val user = it.getValue(UserItem::class.java)
                    Log.e("리뷰 이미지", "userId: ${user?.userId}, reviewsId: ${reviews.userId}")
                    if (user?.userId == reviews.userId) {

                        Glide.with(binding.reviewsWriteImageView)
                            .load(Uri.parse(user?.userImage))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .fitCenter()
                            .circleCrop()
                            .into(binding.reviewsWriteImageView)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    fun delete() {
        var currentReview = ""
        Thread {
            ReviewDatabase.getInstance(this)?.reviewsDao()?.delete(reviews)

            runOnUiThread {
                reviewsAdapter.list.remove(reviews)
                currentReviewDB.get().addOnSuccessListener {
                    val review = it.getValue(Reviews::class.java)
                    currentReview = review?.reviewId.toString()
                }

                Log.e("reviewId", "current: ${currentReview}, reviewsid: ${reviews.reviewId}")
                currentReviewDB.child(currentReview).child(reviews.reviewId!!).removeValue()
                reviewsAdapter.notifyDataSetChanged()
                val intent = Intent().putExtra("isDelete", true)
                setResult(RESULT_OK, intent)
                Toast.makeText(this, "삭제가 완료됐습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.start()
    }

//    private fun updateReview() {
//        if(reviews.reviewId != "") {
//            binding.reviewsWriteDetailTextView.isFocusableInTouchMode = true
//            binding.reviewsWriteDetailTextView.isFocusable = true
//            binding.reviewsWriteDetailTextView.selectionEnd
//
//            val review = mutableMapOf<String, Any>()
//            review["detail"] = binding.reviewsWriteDetailTextView.text
//
//            Firebase.database(Key.DB_URL).reference.child(Key.DB_REVIEWS).child(currentUserId!!).updateChildren(review)
//        }
//
//        Toast.makeText(this, "리뷰 작성 완료!", Toast.LENGTH_SHORT).show()
//    }


    companion object {
        const val EXTRA_REVIEW_ID = "REVIEW_ID"
    }
}