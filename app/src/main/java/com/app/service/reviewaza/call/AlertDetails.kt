package com.app.service.reviewaza.call

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.service.reviewaza.CALL_RESPONSE
import com.app.service.reviewaza.R
import com.app.service.reviewaza.databinding.ActivityFcmDialogBinding
import com.app.service.reviewaza.login.UserItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class
AlertDetails : AppCompatActivity() {

    private lateinit var binding: ActivityFcmDialogBinding

    private var userId: String = ""
    private var otherUserFcmToken: String = ""
    private var chatRoomId: String = ""
    private val chatItemList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFcmDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId")!!
        val myUserName = intent.getStringExtra("myUserName")
        var userImage = intent.getStringExtra("userImage")
        val userMessage = intent.getStringExtra("userMessage")
        val currentLocation = intent.getStringExtra("currentLocation")
        val destination = intent.getStringExtra("destination")
        chatRoomId = intent.getStringExtra("chatRoomId")!!

        var taxiUserName = intent.getStringExtra("taxiUserName")

        binding.callUserNameValue.setText("${myUserName}")
        binding.callUserMessageValue.setText("${userMessage}")
        binding.callCurrentLocationValue.setText("${currentLocation}")
        binding.callDetinationValue.setText("${destination}")

        getOtherUserData()

        if(CALL_RESPONSE.equals("YES_RESPONSE") || CALL_RESPONSE.equals("NO_RESPONSE")){

            if(userImage == null) {
                Glide.with(binding.fcmUserImageView)
                    .load(R.drawable.ic_baseline_person_24)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .circleCrop()
                    .into(binding.fcmUserImageView)
            } else {
                Glide.with(binding.fcmUserImageView)
                    .load(Uri.parse(userImage))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .circleCrop()
                    .into(binding.fcmUserImageView)
            }
            binding.callUserName.setText("기사 ")
            binding.callUserNameValue.setText(taxiUserName)
            binding.callCurrentLocationValue.isVisible = false
            binding.callCurrentLocation.isVisible = false
            binding.callDestination.isVisible = false
            binding.callDetinationValue.isVisible = false
            binding.callUserMessage.setText("호출 응답")

            Toast.makeText(this, "${userImage}", Toast.LENGTH_SHORT).show()
        }

        val currentDB = Firebase.database.reference.child(Key.DB_USERS)
        val currentUserId = Firebase.auth.currentUser?.uid
        currentDB.child(currentUserId!!).get().addOnSuccessListener {
            val user = it.getValue(UserItem::class.java)
            taxiUserName = user?.username.toString()
            userImage = user?.userImage.toString()
        }

        binding.fcmPosButton.setOnClickListener {

            val client = OkHttpClient()

            val root = JSONObject()
            val notification = JSONObject()
            val data = JSONObject()
            notification.put("title", getString(R.string.app_name))
            notification.put("body", "응답 수락")
            data.put("call", "YES_RESPONSE")
            data.put("taxiUserName", taxiUserName)
            data.put("userImage", userImage)
            root.put("to", otherUserFcmToken)
            root.put("priority", "high")
            root.put("notification", notification)
            root.put("data", data)

            val requestBody =
                root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request =
                Request.Builder().post(requestBody).url("https://fcm.googleapis.com/fcm/send")
                    .header("Authorization", "key=${getString(R.string.FCM_SERVER_KEY)}").build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.stackTraceToString()
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.e("AlertDetail", response.toString())
                }

            })

            finish()
        }

        binding.fcmNegButton.setOnClickListener {
            val client = OkHttpClient()

            val root = JSONObject()
            val notification = JSONObject()
            val data = JSONObject()
            notification.put("title", getString(R.string.app_name))
            notification.put("body", "응답 거절")
            data.put("call", "NO_RESPONSE")
            data.put("taxiUserName", taxiUserName)
            data.put("userImage", userImage)
            root.put("to", otherUserFcmToken)
            root.put("priority", "high")
            root.put("notification", notification)
            root.put("data", data)

            val requestBody =
                root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request =
                Request.Builder().post(requestBody).url("https://fcm.googleapis.com/fcm/send")
                    .header("Authorization", "key=${getString(R.string.FCM_SERVER_KEY)}").build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.stackTraceToString()
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.e("AlertDetail", response.toString())
                }

            })

            finish()
        }

        if (userImage != "") {

            if (userImage == null) {
                Glide.with(binding.fcmUserImageView)
                    .load(R.drawable.ic_baseline_person_24)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .circleCrop()
                    .into(binding.fcmUserImageView)
            } else {
                Glide.with(binding.fcmUserImageView)
                    .load(Uri.parse(userImage))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .circleCrop()
                    .into(binding.fcmUserImageView)
            }
        }
    }

    private fun getOtherUserData() {
        Firebase.database.reference.child(Key.DB_USERS).child(userId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)
                otherUserFcmToken = otherUserItem?.fcmToken.orEmpty()
                Log.e("alert otherFcmToken", "${otherUserFcmToken} and ${userId}")

                getChatData()
            }
    }

    private fun getChatData() {
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

