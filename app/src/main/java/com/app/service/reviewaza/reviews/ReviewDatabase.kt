package com.app.service.reviewaza.reviews

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Reviews::class], version = 4)
abstract class ReviewDatabase : RoomDatabase() {

    abstract fun reviewsDao(): ReviewsDao

    companion object {
        private var INSTANCE: ReviewDatabase? = null
        fun getInstance(context: Context): ReviewDatabase? {
            if (INSTANCE == null) {
                synchronized(ReviewDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        ReviewDatabase::class.java,
                        "review-database.db"
                    ).addMigrations(MIGRATION_1_4)
                        .build()
                }
            }
            return INSTANCE
        }

        private val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'review' ADD COLUMN 'thumbUp' INTEGER")
                database.execSQL("ALTER TABLE 'review' ADD COLUMN 'thumbDown' INTEGER")
            }

        }
    }

}