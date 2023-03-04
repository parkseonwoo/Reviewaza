package com.app.service.reviewaza.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.service.reviewaza.MY_STATE
import com.app.service.reviewaza.R
import com.app.service.reviewaza.chatbot.ChatActivity
import com.app.service.reviewaza.databinding.FragmentMypageBinding
import com.app.service.reviewaza.login.LoginActivity

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var binding: FragmentMypageBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMypageBinding.bind(view)

        binding.myPageMyInfo.setOnClickListener {
            if(MY_STATE == "") {
                val intent = Intent(context, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(context, MyPageInfoActivity::class.java)
                startActivity(intent)
            }

        }

        binding.myPageMyReviews.setOnClickListener {
            if(MY_STATE == "") {
                Toast.makeText(context, "로그인을 먼저 해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(context, MyPageMyReviewsActivity::class.java)
                startActivity(intent)
            }
        }

        binding.myPageChatbot.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            startActivity(intent)
        }

    }

}