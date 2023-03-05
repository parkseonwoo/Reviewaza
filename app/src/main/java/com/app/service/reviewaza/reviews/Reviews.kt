package com.app.service.reviewaza.reviews

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "review")
data class Reviews(
    var reviewId: String? = null,
    val rating: Float? = null,
    val taxiType: String? = null,
    val taxiNumber: String? = null,
    val detail: String? = null,
    val currentTime: String? = null,
    val userEmail: String? = null,
    val userId: String? = null,
    var thumbUp: Int? = 0,
    var thumbDown: Int? = 0,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
) : Parcelable
