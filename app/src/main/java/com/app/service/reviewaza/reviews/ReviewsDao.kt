package com.app.service.reviewaza.reviews

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReviewsDao {
    @Query("SELECT * from reviews ORDER BY id DESC")
    fun getAll(): List<Reviews>

    @Query("SELECT * from reviews ORDER BY id DESC LIMIT 1")
    fun getLatesReviews() : Reviews

    @Query("SELECT * FROM reviews ORDER BY rating DESC")
    fun getHigerReviews() : List<Reviews>

    @Query("SELECT * FROM reviews ORDER BY rating ASC")
    fun getLowerReviews() : List<Reviews>

    @Query("SELECT * FROM reviews WHERE userEmail = :user_email")
    fun getMyReviews(user_email: String) : List<Reviews>

    @Insert
    fun insert(reviews: Reviews)

    @Delete
    fun delete(reviews: Reviews)

    @Update
    fun update(reviews: Reviews)
}