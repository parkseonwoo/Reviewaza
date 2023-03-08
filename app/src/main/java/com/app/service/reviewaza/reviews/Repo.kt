package com.app.service.reviewaza.reviews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.service.reviewaza.call.Key
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class Repo {

    private val reviewDB = Firebase.database.reference.child(Key.DB_REVIEWS)
    private val currentReviewDB = Firebase.database.reference.child(Key.DB_REVIEWS)

    private lateinit var reviewsAdapter: ReviewListAdapter

    val mutableData = MutableLiveData<MutableList<Reviews>>()

    fun getFirebaseData(): LiveData<MutableList<Reviews>> {

        reviewsAdapter = ReviewListAdapter(mutableListOf()) {

        }

        reviewDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reviewsList = mutableListOf<Reviews>()
                if(snapshot.exists()) {
                    for(reviewSnapshot in snapshot.children) {
                        Log.e("리뷰 데이터 변경", "getFirebaseData : ${reviewSnapshot}")
                        val getData = reviewSnapshot.getValue(Reviews::class.java)
                        reviewsList.add(getData!!)
                        mutableData.value = reviewsList
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        Log.e("리뷰 데이터 변경", "getFirebaseData : ${mutableData}")
        return mutableData
    }

    fun add(reviews: Reviews) {
        Log.e("리뷰 데이터 변경", "add : 시작")
        reviewsAdapter = ReviewListAdapter(mutableListOf()) {

        }
        currentReviewDB.push().apply {

            val newReview = Reviews(
                reviewId = reviews.reviewId,
                rating = reviews.rating,
                taxiType = reviews.taxiType,
                taxiNumber = reviews.taxiNumber,
                detail = reviews.detail,
                currentTime = reviews.currentTime,
                userEmail = reviews.userEmail,
                userId = reviews.userId,
                thumbUp = reviews.thumbUp,
                thumbDown = reviews.thumbDown
            )

            newReview.reviewId = key!!
            setValue(newReview)

        }


    }


}