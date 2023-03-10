package com.app.service.reviewaza.reviews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.service.reviewaza.call.Key
import com.google.firebase.auth.FirebaseAuth
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

    private var myUsername = FirebaseAuth.getInstance().currentUser?.email

    private lateinit var reviews: Reviews

    val mutableData = MutableLiveData<MutableList<Reviews>>()

    fun getFirebaseData(): LiveData<MutableList<Reviews>> {

        reviewsAdapter = ReviewListAdapter(mutableListOf()) { }

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

    fun getMyReviewFirebaseData(): LiveData<MutableList<Reviews>> {

        reviewsAdapter = ReviewListAdapter(mutableListOf()) { }

        reviewDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reviewsList = mutableListOf<Reviews>()
                if(snapshot.exists()) {
                    for (reviewSnapshot in snapshot.children) {
                        val review = reviewSnapshot.getValue(Reviews::class.java)

                        if (review?.userEmail == myUsername) {
                            Log.e("리뷰 데이터 변경", "getMyReviewFirebaseData : ${reviewSnapshot}")
                            reviewsList.add(review!!)
                            mutableData.value = reviewsList
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        Log.e("리뷰 데이터 변경", "getMyReviewFirebaseData : ${mutableData}")
        return mutableData
    }

    fun add(reviews: Reviews) {
        Log.e("리뷰 데이터 변경", "add : 시작")
        reviewsAdapter = ReviewListAdapter(mutableListOf()) { }
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

    fun delete(reviews: Reviews) {

        var currentReview = ""

        reviewsAdapter = ReviewListAdapter(mutableListOf()) { }

        Log.e("리뷰 데이터 변경", "delete : 시작")

        currentReviewDB.get().addOnSuccessListener {
            val review = it.getValue(Reviews::class.java)
            currentReview = review?.reviewId.toString()
        }

        Log.e("reviewId", "current: ${currentReview}, reviewsid: ${reviews.reviewId}")
        currentReviewDB.child(currentReview).child(reviews.reviewId!!).removeValue()

    }


}