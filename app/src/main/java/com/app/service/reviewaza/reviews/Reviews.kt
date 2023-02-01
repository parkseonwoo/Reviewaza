package com.app.service.reviewaza.reviews

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat

@Parcelize
@Entity(tableName = "reviews")
data class Reviews(
    val taxiType: String,
    val taxiNumber: String,
    val detail: String,
    val currentTime: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
) : Parcelable
