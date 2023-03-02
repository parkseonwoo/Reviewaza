package com.app.service.reviewaza.call


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.service.reviewaza.R
import com.app.service.reviewaza.databinding.ActivityFcmDialogBinding
import com.app.service.reviewaza.login.UserItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_fcm_dialog.*

class AlertDetails : AppCompatActivity() {

    private lateinit var binding: ActivityFcmDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFcmDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("userId")
        val myUserName = intent.getStringExtra("myUserName")
        val userImage = intent.getStringExtra("userImage")
        val userMessage = intent.getStringExtra("userMessage")

        binding.callUserNameValue.setText("${userId}")
        binding.callUserMessageValue.setText("${userMessage}")

        binding.fcmPosButton.setOnClickListener {
            finish()
        }

        if (userImage != "") {
            Toast.makeText(this, "왜 안될까? ${userImage}", Toast.LENGTH_SHORT).show()

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
}

