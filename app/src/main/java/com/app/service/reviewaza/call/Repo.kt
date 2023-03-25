package com.app.service.reviewaza.call

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.service.reviewaza.login.UserItem
import com.app.service.reviewaza.reviews.ReviewListAdapter
import com.app.service.reviewaza.reviews.Reviews
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

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

    fun getChatRoomDB(otherUser: UserItem): String {
        val chatRoomDB = Firebase.database.reference.child(Key.DB_CALLS).child(currentUserId).child(otherUser.userId ?: "")
        var callId = ""
        chatRoomDB.get().addOnSuccessListener {

            if(it.value != null) {

                val chatRoom = it.getValue(CallItem::class.java)
                callId = chatRoom?.callId ?: ""
            } else {

                callId = UUID.randomUUID().toString()
                val newChatRoom = CallItem(
                    callId = callId,
                    otherUserName = otherUser.username,
                    otherUserId = otherUser.userId,
                )
                chatRoomDB.setValue(newChatRoom)
            }
        }

        return callId
    }

    fun getChatData(chatRoomId: String) {

        val chatItemList = mutableListOf<ChatItem>()
        Firebase.database.reference.child(Key.DB_CHATS).child(chatRoomId).addChildEventListener(
            object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatItem = snapshot.getValue(ChatItem::class.java)
                    chatItem ?: return

                    chatItemList.add(chatItem)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}

            })
    }

}