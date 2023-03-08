package com.app.service.reviewaza.call

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.service.reviewaza.login.UserItem
import com.app.service.reviewaza.reviews.ReviewListAdapter
import com.app.service.reviewaza.reviews.Reviews
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Repo {
    val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    val mutableData = MutableLiveData<MutableList<UserItem>>()

    private lateinit var callListAdapter: CallListAdapter

    fun getFirebaseData(): LiveData<MutableList<UserItem>> {

        callListAdapter = CallListAdapter {  }

        Firebase.database.reference.child(Key.DB_USERS)
            .addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val userItemList = mutableListOf<UserItem>()

                    snapshot.children.forEach {
                        val user = it.getValue(UserItem::class.java)
                        user ?: return

                        if(user.userId != currentUserId) {
                            userItemList.add(user)
                            mutableData.value = userItemList
                            Log.e("callRepo", "${user.userId}")
                        }

                        callListAdapter.submitList(userItemList)
                    }

                }

                override fun onCancelled(error: DatabaseError) {}

            })

        return mutableData
    }

}