package com.app.service.reviewaza.reviews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*


class ReviewsDetailActivity : AppCompatActivity(), ReviewsAdapter.ItemClickListener {

    private lateinit var binding: ActivityReviewsDetailBinding
    private lateinit var reviews: Reviews
    private lateinit var reviewsAdapter: ReviewsAdapter

    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""
    private val currentUserDB = Firebase.database.reference.child(Key.DB_USERS)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityReviewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // deprecated
        // reviews = intent.getSerializableExtra("reviews") as Reviews

        reviews = intent.getParcelableExtra("reviews")!!

        if (currentUserId != null) {
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

        currentUserDB.addListenerForSingleValueEvent(object: ValueEventListener { // 유저 내요이 많이 바뀌지 않기 때문에 addListenerForSingleValueEvent로 부름

            override fun onDataChange(snapshot: DataSnapshot) { // snapshot은 uid로 가져온 데이터 하나하나를 의미

                snapshot.children.forEach {
                    val user = it.getValue(UserItem::class.java)
                    Log.e("리뷰 이미지", "userId: ${user?.userId}, reviewsId: ${reviews.userId}")
                    if(user?.userId == reviews.userId) {
                        Log.e("리뷰 이미지", "username: ${user.username}, reviewsEmail: ${reviews.userEmail}")
                        Glide.with(binding.reviewsWriteImageView)
                            .load(Uri.parse(user?.userImage))
                            .override(350, 350)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(binding.reviewsWriteImageView)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })
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