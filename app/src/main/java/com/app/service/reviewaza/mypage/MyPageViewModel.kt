package com.app.service.reviewaza.mypage

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import com.app.service.reviewaza.reviews.ReviewDatabase
import com.app.service.reviewaza.reviews.Reviews
import kotlinx.coroutines.launch

class MyPageViewModel(application: Application) : ViewModel() {

    private val getAllData : LiveData<List<Reviews>>
    private val repo: Repo

    init {
        val reviewsDao = ReviewDatabase.getInstance(application)!!.reviewsDao()
        repo = Repo(reviewsDao)
        getAllData = repo.getAllData
    }

    fun insert(reviews: Reviews) = viewModelScope.launch {
        repo.insert(reviews)
    }

    fun delete(reviews: Reviews) = viewModelScope.launch {
        repo.delete(reviews)
    }

    class Factory(val application: Application) :ViewModelProvider.Factory {
        override fun <T: ViewModel> create(modelClass: Class<T>) : T {
            return MyPageViewModel(application) as T
        }
    }
}

