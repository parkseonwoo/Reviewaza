package com.app.service.reviewaza.reviews

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Reviews::class], version = 1)
abstract class ReviewDatabase : RoomDatabase() {

    abstract fun reviewsDao(): ReviewsDao

    companion object {
        private var INSTANCE : ReviewDatabase? = null
        fun getInstance(context: Context) : ReviewDatabase? {
            if(INSTANCE == null) {
                synchronized(ReviewDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        ReviewDatabase::class.java,
                        "review-database.db"
                    ).build()
                }
            }
            return INSTANCE
        }

    }

}