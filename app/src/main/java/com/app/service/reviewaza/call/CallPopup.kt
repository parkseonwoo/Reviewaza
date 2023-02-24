package com.app.service.reviewaza.call

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CallPopup :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val builder = AlertDialog.Builder(this)
//            .setTitle("타이틀")
//            .setMessage("메시지")
//            .setPositiveButton("확인", DialogInterface.OnClickListener{ dialog, witch ->
//                Toast.makeText(this,"확인",Toast.LENGTH_SHORT).show()
//            })
//            .setNegativeButton("취소", null)
//            builder.show()
        initDialog()
        Log.e("CallPopup", "확인")

    }

    private fun initDialog() {
        val callRequest = intent.extras
        if(callRequest != null) {
            Toast.makeText(this, "정상적으로 팝업이 표출됩니다", Toast.LENGTH_SHORT).show()
        }
    }
}