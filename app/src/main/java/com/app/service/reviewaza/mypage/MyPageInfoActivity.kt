package com.app.service.reviewaza.mypage

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.service.reviewaza.*
import com.app.service.reviewaza.databinding.ActivityMyPageInfoBinding
import com.app.service.reviewaza.databinding.ActivityMypageEditDialogBinding
import kotlinx.android.synthetic.main.activity_mypage_edit_dialog.view.*

class MyPageInfoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMyPageInfoBinding

    // 갤러리 이미지 로드하는 런처
    private val imageLoadLauncher = registerForActivityResult(ActivityResultContracts.GetContent() ) { uri ->
        if (uri != null) {
            updateImages(uri)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(LOGIN_EMAIL != null) binding.emailValueTextView.text = LOGIN_EMAIL

        binding.logoutButton.setOnClickListener {
            LOGIN_VALUE = 0
            finish()
        }

        binding.myPageImage.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setMessage("이미지 추가")
                setNegativeButton("취소", null)
                setPositiveButton("확인") { _, _ ->
                    checkPermission()
                }.show()
            }
        }

        binding.nicknameEditHelper.setOnClickListener {
            showAlertDialog()
        }

    }

    override fun onResume() {
        binding.nicknameTextView.text = MYPAGE_NICKNAME_VALUE

        super.onResume()
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
                    MYPAGE_NICKNAME_VALUE = editText.textInputEditText.text.toString()
                    binding.nicknameTextView.setText(MYPAGE_NICKNAME_VALUE)
                    setResult(RESULT_OK, intent)
                    Toast.makeText(applicationContext, "변경된 이름은 ${editText.textInputEditText} 입니다", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("아니요", null)
            show()
        }
    }


    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadImage()
            }
            shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                showPermissionInfoDialog()
            }
            else -> {
                requestReadExternalStorage()
            }
        }
    }

    private fun showPermissionInfoDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this).apply {
            setMessage("이미지를 가져오기 위해서, 외부 저장소 읽기 권한이 필요합니다")
            setNegativeButton("취소", null)
            setPositiveButton("동의") { _, _ ->
                requestReadExternalStorage()
            }
        }.show()
    }

    private fun loadImage() {
        imageLoadLauncher.launch("image/*")
    }

    private fun requestReadExternalStorage() {
        // (Context, 필요한 권한(여러개 가능)), 요청 코드
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_READ_EXTERNAL_STORAGE
        )
    }

    private fun updateImages(uri : Uri) {
        val images = uri
        binding.myPageImage.setImageURI(images)
    }

    override fun onRequestPermissionsResult( // 권한을 동의하게 된다면 바로 이미지를 로드할 수 있게 해주는 기능
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                val resultCode = grantResults.firstOrNull() ?: PackageManager.PERMISSION_DENIED
                if(resultCode == PackageManager.PERMISSION_GRANTED) {
                    loadImage()
                }
            }
        }
    }

    companion object {
        const val REQUEST_READ_EXTERNAL_STORAGE = 100
    }

    sealed class ImageItems {
        data class Image(
            val uri : Uri,
        ) : ImageItems()

        // 싱글톤으로 바로 객체가 만들어지는 특징이 있다
        object LoadMore : ImageItems()
    }

}