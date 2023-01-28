package com.app.service.reviewaza.mypage

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.app.service.reviewaza.LOGIN_VALUE
import com.app.service.reviewaza.databinding.ActivityMyPageInfoBinding
import com.app.service.reviewaza.databinding.ActivityMypageEditDialogBinding
import kotlinx.android.synthetic.main.activity_mypage_edit_dialog.view.*

class MyPageInfoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMyPageInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logoutButton.setOnClickListener {
            LOGIN_VALUE = 0
            finish()
        }

        binding.nicknameEditImageView.setOnClickListener {
            showAlertDialog()
        }

    }

    private fun showAlertDialog() {

        val builder = AlertDialog.Builder(this)
        val builderItem = ActivityMypageEditDialogBinding.inflate(layoutInflater)
        val editText = builderItem.editTextInputLayout

        with(builder) {
            setTitle("변경내용")
            setMessage("변경할 내용을 입력하세요")
            setView(builderItem.root)
            setPositiveButton("네"){dialog, id ->
                if(editText.textInputEditText != null) {
                    binding.nicknameTextView.text = editText.textInputEditText.text
                    Toast.makeText(applicationContext, "변경된 이름은 ${editText.textInputEditText} 입니다", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("아니요", null)
            show()
        }
    }
}