package com.app.service.reviewaza.reviews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.app.service.reviewaza.REVIEWS_DETAIL_FLAG
import com.app.service.reviewaza.call.Key
import com.app.service.reviewaza.databinding.ActivityReviewsDetailBinding
import com.app.service.reviewaza.login.UserItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
    private val reviewThumbUp = mutableMapOf<String, Any>()
    private val reviewThumbDown = mutableMapOf<String, Any>()

    private val reviewListViewModel by lazy { ViewModelProvider(this).get(ReviewListViewModel::class.java) }

    private val startMyReviews = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isUpdate = result.data?.getBooleanExtra("isUpdate", false) ?: false

        if (result.resultCode == RESULT_OK && isUpdate) {
            initView()
        }
    }

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
            binding.thumbUp.isVisible = false
            binding.thumbUpValue.isVisible = false
            binding.thumbDown.isVisible = false
            binding.thumbDownValue.isVisible = false
        }

        binding.reviewsEditButton.setOnClickListener {
            Toast.makeText(this, "수정 버튼 클릭", Toast.LENGTH_SHORT).show()
            updateReview()
        }

        binding.reviewsDeleteButton.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setMessage("정말 삭제하시겠습니까?")
                setNegativeButton("취소", null)
                setPositiveButton("확인") { _, _ ->
                    if (!NetworkManager.checkNetworkState(context)) {
                        Toast.makeText(context, "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show()
                    } else delete()
                }.show()
            }
        }

        binding.thumbUp.setOnClickListener {
            var thumbUp:Int = 0

            currentReviewDB.child(reviews.reviewId!!).get().addOnSuccessListener {
                val review = it.getValue(Reviews::class.java)
                thumbUp = review?.thumbUp!!
                Log.e("thumbUp1:", "${thumbUp}")

                thumbUp++
                Log.e("thumbUp2:", "${thumbUp}")

                binding.thumbUpValue.setText("${thumbUp}")
                reviewThumbUp["thumbUp"] = thumbUp
                //binding.thumbUpValue.setText(thumbUp)
                Log.e("currentReviewId:", "${reviews.reviewId}")

                currentReviewDB.child(reviews.reviewId!!).updateChildren(reviewThumbUp)
            }
        }

        binding.thumbDown.setOnClickListener {
            var thumbDown:Int = 0

            currentReviewDB.child(reviews.reviewId!!).get().addOnSuccessListener {
                val review = it.getValue(Reviews::class.java)
                thumbDown = review?.thumbDown!!

                thumbDown++
                Log.e("thumbUp2:", "${thumbDown}")

                binding.thumbDownValue.setText("-${thumbDown}")
                reviewThumbDown["thumbDown"] = thumbDown

                currentReviewDB.child(reviews.reviewId!!).updateChildren(reviewThumbDown)
            }
        }

        initView()

    }

    private fun initView() {
        reviewsAdapter = ReviewListAdapter(mutableListOf()) {
            startMyReviews.launch(intent)
        }
        binding.reviewsUserEmail.text = reviews.userEmail
        binding.reviewsWriteRatingBar.rating = reviews.rating!!
        binding.reviewsWriteTaxiTypeValueTextView.text = reviews.taxiType
        binding.reviewsWriteTaxiNumberValueTextView.text = reviews.taxiNumber
        binding.reviewsWriteDetailTextView.setText(reviews.detail)

        binding.thumbUpValue.setText("${reviews.thumbUp}")
        binding.thumbDownValue.setText("-${reviews.thumbDown}")

        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val reviewsList = mutableListOf<Reviews>()

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
                reviewsAdapter.submitList(reviewsList)

            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    fun delete() {
        Thread{
            ReviewDatabase.getInstance(this)?.reviewsDao()?.delete(reviews)
        }.start()
        reviewListViewModel.deleteData(reviews)
        Toast.makeText(this, "삭제가 완료됐습니다", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateReview() {
        binding.updateReviewOkButton.isVisible = true
        binding.reviewsDeleteButton.isVisible = false
        binding.reviewsEditButton.isVisible = false

        binding.reviewsWriteRatingBar.setIsIndicator(false)
        binding.reviewsWriteRatingBar.isEnabled = true
        binding.reviewsWriteRatingBar.isClickable = true
        binding.reviewsWriteRatingBar.isFocusableInTouchMode = true

        binding.reviewsWriteDetailTextView.isEnabled = true
        binding.reviewsWriteDetailTextView.isClickable = true
        binding.reviewsWriteDetailTextView.isFocusableInTouchMode = true
        binding.reviewsWriteDetailTextView.focusable

        binding.updateReviewOkButton.setOnClickListener {
            val reviewsList = mutableListOf<Reviews>()
            currentReviewDB.get().addOnSuccessListener {
                val updates = Reviews(
                    rating = binding.reviewsWriteRatingBar.rating,
                    taxiType = reviews.taxiType,
                    taxiNumber = reviews.taxiNumber,
                    userEmail = reviews.userEmail,
                    currentTime = reviews.currentTime,
                    reviewId = reviews.reviewId,
                    userId = reviews.userId,
                    detail = binding.reviewsWriteDetailTextView.text.toString()
                )
                currentReviewDB.child(reviews.reviewId!!).setValue(updates)
            }

            val intent = Intent().putExtra("isUpdate", true)
            setResult(RESULT_OK, intent)
            reviewsAdapter.submitList(reviewsList)
            Toast.makeText(this, "리뷰 수정 완료!", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    companion object {
        const val EXTRA_REVIEW_ID = "REVIEW_ID"
    }
}