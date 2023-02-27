package com.app.service.reviewaza.reviews

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "reviews")
data class Reviews(
    val rating: Float,
    val taxiType: String,
    val taxiNumber: String,
    val detail: String,
    val currentTime: String,
    val userEmail: String,
    val userId: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
) : Parcelable
