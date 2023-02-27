package com.app.service.reviewaza.reviews

import android.content.Context
import android.widget.AdapterView
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Reviews::class], version = 5)
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
                    ).addMigrations(MIGRATION_4_5)
                        .build()
                }
            }
            return INSTANCE
        }
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'reviews' ADD COLUMN 'userId' TEXT NOT NULL Default '0'")
            }

        }
    }

}