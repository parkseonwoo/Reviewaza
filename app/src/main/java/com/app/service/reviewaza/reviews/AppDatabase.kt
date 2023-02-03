package com.app.service.reviewaza.reviews

import android.content.Context
import android.widget.AdapterView
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Reviews::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reviewsDao(): ReviewsDao

    companion object {
        private var INSTANCE : AppDatabase? = null
        fun getInstance(context: Context) : AppDatabase? {
            if(INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app-database.db"
                    ).addMigrations(MIGRATION_2_3)
                        .build()
                }
            }
            return INSTANCE
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'reviews' ADD COLUMN 'rating' FLOAT NOT NULL DEFAULT 0.0")
            }

        }
    }

}