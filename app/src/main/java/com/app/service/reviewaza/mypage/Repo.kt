package com.app.service.reviewaza.mypage

import androidx.lifecycle.LiveData
import com.app.service.reviewaza.reviews.Reviews
import com.app.service.reviewaza.reviews.ReviewsDao

class Repo(private val reviewsDao: ReviewsDao) {

    val getAllData: LiveData<List<Reviews>> = reviewsDao.getAll()

    suspend fun insert(reviews: Reviews) {
        reviewsDao.insert(reviews)
    }

    suspend fun delete(reviews: Reviews) {
        reviewsDao.delete(reviews)
    }
}