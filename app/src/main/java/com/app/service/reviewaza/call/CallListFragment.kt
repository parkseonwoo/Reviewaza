package com.app.service.reviewaza.call

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.service.reviewaza.R
import com.app.service.reviewaza.call.Key.Companion.DB_USERS
import com.app.service.reviewaza.databinding.FragmentCalllistBinding
import com.app.service.reviewaza.login.UserItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class CallListFragment : Fragment(R.layout.fragment_calllist) {

    private lateinit var binding : FragmentCalllistBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCalllistBinding.bind(view)

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

                val intent = Intent(context, CallActivity::class.java)
                intent.putExtra(CallActivity.EXTRA_CALL_ID, callId)
                intent.putExtra(CallActivity.EXTRA_OTHER_USER_ID, otherUser.userId)
                startActivity(intent)
            }
        }

        binding.toolBar.apply {
            title = "호출 목록"
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