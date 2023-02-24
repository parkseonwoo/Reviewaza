package com.app.service.reviewaza.call

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.call.Key.Companion.DB_USERS
import com.app.service.reviewaza.databinding.ActivityCalllistBinding
import com.app.service.reviewaza.login.UserItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class CallListActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCalllistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalllistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callListAdapter = CallListAdapter { otherUser ->
            val myUserId = Firebase.auth.currentUser?.uid ?: ""
            val chatRoomDB = Firebase.database.reference.child(Key.DB_CALLS).child(myUserId).child(otherUser.userId ?: "")

            chatRoomDB.get().addOnSuccessListener {
                var callId = ""
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

                val intent = Intent(this, CallActivity::class.java)
                intent.putExtra(CallActivity.EXTRA_CALL_ID, callId)
                intent.putExtra(CallActivity.EXTRA_OTHER_USER_ID, otherUser.userId)
                startActivity(intent)
            }
        }

        binding.callListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = callListAdapter
        }

        val currentUserId = Firebase.auth.currentUser?.uid ?: ""

        Firebase.database.reference.child(DB_USERS)
            .addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val userItemList = mutableListOf<UserItem>()

                    snapshot.children.forEach {
                        val user = it.getValue(UserItem::class.java)
                        user ?: return

                        if(user.userId != currentUserId) {
                            userItemList.add(user)
                        }
                    }

                    callListAdapter.submitList(userItemList)
                }

                override fun onCancelled(error: DatabaseError) {}

            })


    }
}