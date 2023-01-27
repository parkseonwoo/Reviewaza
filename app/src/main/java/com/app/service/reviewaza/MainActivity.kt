package com.app.service.reviewaza

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.app.service.reviewaza.databinding.ActivityMainBinding
import com.app.service.reviewaza.login.LoginActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("imageConvert", "onCreate")
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainLogin.setOnClickListener {
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        getDataUiUpdate()
    }

    private fun getDataUiUpdate() {
        if(LOGIN_VALUE != 0) {
            //binding.mainLogin.setImageResource(R.drawable.ic_baseline_person_24)
            var intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }
    }
}