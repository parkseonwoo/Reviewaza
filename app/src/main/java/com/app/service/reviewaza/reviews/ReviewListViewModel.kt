package com.app.service.reviewaza.reviews

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReviewListViewModel : ViewModel() {

    private val repo = Repo()

    fun fetchData(): LiveData<MutableList<Reviews>> {
        val mutableData = MutableLiveData<MutableList<Reviews>>()
        repo.getFirebaseData().observeForever{
            Log.e("리뷰 데이터 변경", "fetchData : 시작")
            mutableData.value = it
        }

        return mutableData
    }

    fun fetchMyReviewData(): LiveData<MutableList<Reviews>> {
        val mutableData = MutableLiveData<MutableList<Reviews>>()
        repo.getMyReviewFirebaseData().observeForever{
            Log.e("내 리뷰 데이터 변경", "fetchMyReviewData : 시작")
            mutableData.value = it
        }

        return mutableData
    }

    fun addData(reviews: Reviews)  {
        Log.e("리뷰 데이터 변경", "addData : 시작")
        repo.add(reviews)
    }

    fun deleteData(reviews: Reviews) {
        Log.e("리뷰 데이터 변경", "deleteData : 시작")
        repo.delete(reviews)
    }

}