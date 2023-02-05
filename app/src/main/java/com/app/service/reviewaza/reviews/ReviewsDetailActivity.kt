package com.app.service.reviewaza.reviews

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.service.reviewaza.LOGIN_EMAIL
import com.app.service.reviewaza.databinding.ActivityReviewsDetailBinding


class ReviewsDetailActivity : AppCompatActivity() {

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

        binding.reviewsEditButton.setOnClickListener {
            Toast.makeText(this, "수정 버튼 클릭", Toast.LENGTH_SHORT).show()
        }

        binding.reviewsDeleteButton.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setMessage("정말 삭제하시겠습니까?")
                setNegativeButton("취소", null)
                setPositiveButton("확인") { _, _ ->
                    delete()
                    finish()
                }.show()
            }
        }

        initView()
    }

    private fun initView() {
        binding.reviewsUserEmail.text = LOGIN_EMAIL
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
                reviewsAdapter.notifyDataSetChanged()
                Toast.makeText(this, "삭제가 완료됐습니다", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

}