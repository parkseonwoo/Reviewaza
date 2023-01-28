package com.app.service.reviewaza.mypage

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.service.reviewaza.LOGIN_VALUE
import com.app.service.reviewaza.databinding.ActivityMypageBinding
import com.app.service.reviewaza.login.LoginActivity

class MyPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMypageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.myPageMyInfo.setOnClickListener {
            if(LOGIN_VALUE == 0) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, MyPageInfoActivity::class.java)
                startActivity(intent)
            }

        }

    }

}