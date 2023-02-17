package com.app.service.reviewaza.reviews

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.service.reviewaza.LOGIN_EMAIL
import com.app.service.reviewaza.LOGIN_VALUE
import com.app.service.reviewaza.databinding.ActivityReviewsDetailBinding


class ReviewsDetailActivity : AppCompatActivity(), ReviewsAdapter.ItemClickListener {

    private lateinit var binding: ActivityReviewsDetailBinding
    private lateinit var reviews: Reviews
    private lateinit var reviewsAdapter: ReviewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityReviewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // deprecated
        // reviews = intent.getSerializableExtra("reviews") as Reviews

        reviews = intent.getParcelableExtra("reviews")!!

//        if(LOGIN_VALUE != 1) {
//            binding.reviewsEditButton.isEnabled = false
//            binding.reviewsDeleteButton.isEnabled = false
//            binding.reviewsEditButton.isVisible = false
//            binding.reviewsDeleteButton.isVisible = false
//        } else {
//            binding.reviewsEditButton.isEnabled = true
//            binding.reviewsDeleteButton.isEnabled = true
//            binding.reviewsEditButton.isVisible = true
//            binding.reviewsDeleteButton.isVisible = true
//        }

        binding.reviewsEditButton.setOnClickListener {
            Toast.makeText(this, "수정 버튼 클릭", Toast.LENGTH_SHORT).show()
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
        reviewsAdapter = ReviewsAdapter(mutableListOf(), this@ReviewsDetailActivity)
        binding.reviewsUserEmail.text = reviews.userEmail
        binding.reviewsWriteRatingBar.rating = reviews.rating
        binding.reviewsWriteTaxiTypeValueTextView.text = reviews.taxiType
        binding.reviewsWriteTaxiNumberValueTextView.text = reviews.taxiNumber
        binding.reviewsWriteDetailTextView.text = reviews.detail
    }

    fun delete() {
        Thread {
            AppDatabase.getInstance(this)?.reviewsDao()?.delete(reviews)
            runOnUiThread {
                reviewsAdapter.list.remove(reviews)
                val intent = Intent().putExtra("isDelete", true)
                setResult(RESULT_OK, intent)
                Toast.makeText(this, "삭제가 완료됐습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.start()
    }

    override fun onClick(reviews: Reviews) {
        TODO("Not yet implemented")
    }

}