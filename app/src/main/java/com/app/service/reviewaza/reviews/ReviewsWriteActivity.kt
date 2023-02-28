package com.app.service.reviewaza.reviews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.app.service.reviewaza.R
import com.app.service.reviewaza.call.Key
import com.app.service.reviewaza.databinding.ActivityReviewsWriteBinding
import com.app.service.reviewaza.login.UserItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReviewsWriteActivity : AppCompatActivity(), ReviewsAdapter.ItemClickListener {

    private lateinit var binding: ActivityReviewsWriteBinding
    private lateinit var reviewsAdapter: ReviewsAdapter

    private var myUsername = FirebaseAuth.getInstance().currentUser?.email
    private var userId = FirebaseAuth.getInstance().currentUser?.uid
    private val currentUserDB = Firebase.database.reference.child(Key.DB_USERS).child(userId!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewsWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        binding.reviewsWriteOkButton.setOnClickListener {
            add()
        }

        binding.reviewsWriteRatingBar.apply {
            rating = 3f
            Toast.makeText(this@ReviewsWriteActivity, "$rating 입니다", Toast.LENGTH_SHORT).show()
        }

    }

    private fun initViews() {
        reviewsAdapter = ReviewsAdapter(mutableListOf(), this@ReviewsWriteActivity)

        currentUserDB.get().addOnSuccessListener {
            val currentUserItem = it.getValue(UserItem::class.java) ?: return@addOnSuccessListener

            if (currentUserItem.userImage != "") {
                binding.reviewsUserEmail.setText(currentUserItem.username)

                Glide.with(binding.reviewsWriteImageView)
                    .load(Uri.parse(currentUserItem.userImage))
                    .override(350, 350)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(binding.reviewsWriteImageView)
            }
        }

        val types = listOf("친절해요", "쾌적해요", "깔끔해요", "보통이였어요", "불친절해요", "별로였어요", "싸가지없어요")
        binding.reviewsChipGroup.apply {
            types.forEach { text ->
                addView(createChip(text))
            }
        }
        binding.reviewsWriteTaxiTypeEditText.addTextChangedListener {
            it?.let { text ->
                binding.reviewsWriteTaxiTypeLayout.error = when (text.length) {
                    0 -> "값을 입력해주세요"
                    1 -> "2자 이상을 입력해주세요"
                    else -> null
                }
            }
        }

        binding.reviewsWriteTaxiNumberEditText.addTextChangedListener {
            it?.let { text ->
                binding.reviewsWriteTaxiNumberLayout.error = when (text.length) {
                    0 -> "값을 입력해주세요"
                    1 -> "2자 이상을 입력해주세요"
                    else -> null
                }
            }
        }

    }

    private fun createChip(text: String): Chip {
        return Chip(this).apply {
            setText(text)
            isCheckable = true
            isCheckable = true
        }
    }

    private fun add() {
        val rating = binding.reviewsWriteRatingBar.rating
        val taxiType = binding.reviewsWriteTaxiTypeEditText.text.toString()
        val taxiNumber = binding.reviewsWriteTaxiNumberEditText.text.toString()
        val detail = binding.reviewsWriteDetailEditTextView.text.toString()
        var currentTime =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val userEmail = myUsername
        val userId = userId
        val reviews =
            Reviews(rating, taxiType, taxiNumber, detail, currentTime, userEmail!!, userId!!)

        Thread {
            AppDatabase.getInstance(this)?.reviewsDao()?.insert(reviews)
            runOnUiThread {
                reviewsAdapter.list.add(reviews)
                val intent = Intent().putExtra("isUpdated", true)
                setResult(RESULT_OK, intent)
                Toast.makeText(this, "저장을 완료했습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.start()
    }

    override fun onClick(reviews: Reviews) {
        TODO("Not yet implemented")
    }


}