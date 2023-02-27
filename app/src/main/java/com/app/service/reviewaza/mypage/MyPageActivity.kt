package com.app.service.reviewaza.mypage

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.service.reviewaza.chatbot.ChatActivity
import com.app.service.reviewaza.databinding.ActivityMypageBinding
import com.app.service.reviewaza.login.LoginActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MyPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMypageBinding
    private val myUserId = Firebase.auth.currentUser?.uid ?: null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.myPageMyInfo.setOnClickListener {
            if(myUserId == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, MyPageInfoActivity::class.java)
                startActivity(intent)
            }

        }

        binding.myPageMyReviews.setOnClickListener {
            if(myUserId == null) {
                Toast.makeText(this, "로그인을 먼저 해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MyPageMyReviewsActivity::class.java)
                startActivity(intent)
            }
        }

        binding.myPageChatbot.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

    }

}