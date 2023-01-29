package com.app.service.reviewaza

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.service.reviewaza.databinding.ActivityMainBinding
import com.app.service.reviewaza.mypage.MyPageActivity
import com.app.service.reviewaza.reviews.LatestReviewsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainMyPageImageButton.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }

        binding.mainReviewImageButton.setOnClickListener {
            val intent = Intent (this, LatestReviewsActivity::class.java)
            startActivity(intent)
        }

    }

}