package com.app.service.reviewaza.call

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.service.reviewaza.login.UserItem

class CallListViewModel : ViewModel() {

    private val repo = com.app.service.reviewaza.call.Repo()

    fun fetchData(): LiveData<MutableList<UserItem>> {
        val mutableData = MutableLiveData<MutableList<UserItem>>()
        repo.getFirebaseData().observeForever{
            mutableData.value = it
        }

        return mutableData
    }

}