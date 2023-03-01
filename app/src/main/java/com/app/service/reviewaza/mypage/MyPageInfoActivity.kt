package com.app.service.reviewaza.mypage

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.app.service.reviewaza.LOGIN_SET
import com.app.service.reviewaza.MY_STATE
import com.app.service.reviewaza.MainActivity
import com.app.service.reviewaza.R
import com.app.service.reviewaza.call.Key.Companion.DB_USERS
import com.app.service.reviewaza.databinding.ActivityMypageEditDialogBinding
import com.app.service.reviewaza.databinding.ActivityMypageInfoBinding
import com.app.service.reviewaza.login.UserItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.activity_mypage_edit_dialog.view.*
import java.text.SimpleDateFormat
import java.util.*

class MyPageInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMypageInfoBinding
    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""
    private val currentUserDB = Firebase.database.reference.child(DB_USERS).child(currentUserId)
    private val user = mutableMapOf<String, Any>()

    private var storage: FirebaseStorage? = FirebaseStorage.getInstance()
    private var fileName = "IMAGE_${SimpleDateFormat("yyyymmdd_HHmmss").format(Date())}_.png"
    private var imagesRef = storage!!.reference.child("images/").child(fileName)

    private var myUserId = Firebase.auth.currentUser?.uid ?: ""
    var flag = false

    // 갤러리 이미지 로드하는 런처
    private val imageLoadLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                //contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                updateImages(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        binding.logoutButton.setOnClickListener {
            userLogout()
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
            showAlertDialog("nickname")
        }

        binding.passwordEditHelper.setOnClickListener {
            showAlertDialog("password")
        }

        binding.phoneNumberEditHelper.setOnClickListener {
            showAlertDialog("phoneNumber")
        }

        binding.deleteUserButton.setOnClickListener {
            userDelete()
        }

    }

    private fun initViews() {

        currentUserDB.get().addOnSuccessListener {
            val currentUserItem = it.getValue(UserItem::class.java) ?: return@addOnSuccessListener

            if (currentUserItem.userImage != "") {
                binding.nicknameValueTextView.setText(currentUserItem.username)

                Toast.makeText(this, "왜 안될까? ${currentUserItem.userImage}", Toast.LENGTH_SHORT)
                    .show()
                if (currentUserItem.userImage == null) {
                    Glide.with(binding.myPageImage)
                        .load(R.drawable.ic_baseline_person_24)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .circleCrop()
                        .into(binding.myPageImage)
                } else {
                    Glide.with(binding.myPageImage)
                        .load(Uri.parse(currentUserItem.userImage))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .circleCrop()
                        .into(binding.myPageImage)
                }
            }

            flag = false

        }

        if (MY_STATE != "") binding.emailValueTextView.text = Firebase.auth.currentUser?.email
    }


    private fun showAlertDialog(action: String) {

        val builder = AlertDialog.Builder(this)
        val builderItem = ActivityMypageEditDialogBinding.inflate(layoutInflater)
        val editText = builderItem.editTextInputLayout

        with(builder) {
            setTitle("변경내용")
            setMessage("변경할 내용을 입력하세요")
            setView(builderItem.root)
            setPositiveButton("네") { dialog, id ->
                if (editText.textInputEditText != null) {
                    //val user = mutableMapOf<String, Any>()
                    if (action == "nickname") {
                        val username = editText.textInputEditText.text.toString()
                        binding.nicknameValueTextView.setText(username)
                        user["username"] = username
                    } else if (action == "password") {
                        val userpassword = editText.textInputEditText.text.toString()
                        binding.passwordValueTextView.setText(userpassword)
                        user["userpassword"] = userpassword
                    } else if (action == "phoneNumber") {
                        val userPhoneNumber = editText.textInputEditText.text.toString()
                        binding.phoneNumberValueTextView.setText(userPhoneNumber)
                        user["userPhoneNumber"] = userPhoneNumber
                    }
                    setResult(RESULT_OK, intent)
                    currentUserDB.updateChildren(user)
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
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_READ_EXTERNAL_STORAGE
        )
    }

    private fun updateImages(uri: Uri) {
        val images = uri
        if (images != null) {
            uploadImageToFirebase(images,
                mSuccessHandler = { imageuri ->
                    user["userImage"] = imageuri
                    currentUserDB.updateChildren(user)
                    flag = true
                },
                mErrorHandler = {
                    Toast.makeText(this, "게시글 업로드에 실패했습니다", Toast.LENGTH_SHORT).show()
                })
        } else {
//            user["userImage"] = uri.toString()
//            currentUserDB.updateChildren(user)
//            flag = true
        }

        binding.myPageImage.setImageURI(images)
    }


    override fun onRequestPermissionsResult( // 권한을 동의하게 된다면 바로 이미지를 로드할 수 있게 해주는 기능
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                val resultCode = grantResults.firstOrNull() ?: PackageManager.PERMISSION_DENIED
                if (resultCode == PackageManager.PERMISSION_GRANTED) {
                    loadImage()
                }
            }
        }
    }

    private fun uploadImageToFirebase(
        uri: Uri,
        mSuccessHandler: (String) -> Unit,
        mErrorHandler: () -> Unit,
    ) {
        imagesRef.putFile(uri!!)
            .addOnCompleteListener {
            if (it.isSuccessful) {
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    mSuccessHandler(uri.toString())
                }.addOnFailureListener {
                    mErrorHandler()
                }

                Toast.makeText(this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show()
            } else {
                mErrorHandler
            }

        }
    }

    companion object {
        const val REQUEST_READ_EXTERNAL_STORAGE = 100
    }

    sealed class ImageItems {
        data class Image(
            val uri: Uri,
        ) : ImageItems()

        // 싱글톤으로 바로 객체가 만들어지는 특징이 있다
        object LoadMore : ImageItems()
    }

    private fun userDelete() {
        if (LOGIN_SET == "Kakao") {
            UserApiClient.instance.unlink { error ->
                if (error != null) {
                    Log.d("카카오로그인", "회원 탈퇴 실패")
                } else {
                    MY_STATE = ""
                    Log.d("카카오로그인", "회원 탈퇴 성공")
                }
            }
        } else {
            FirebaseAuth.getInstance().currentUser!!.delete().addOnCompleteListener { task ->
                Toast.makeText(this, "아이디 삭제가 완료되었습니다", Toast.LENGTH_SHORT).show()
                Firebase.auth.signOut()
                finish()
            }
        }
    }

    private fun userLogout() {
        if (LOGIN_SET == "Kakao") {
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Toast.makeText(this, "로그아웃 실패 $error", Toast.LENGTH_SHORT).show()
                } else {
                    MY_STATE = ""
                    Toast.makeText(this, "로그아웃 성공", Toast.LENGTH_SHORT).show()
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                finish()
            }
        } else {
            Firebase.auth.signOut()
            myUserId = ""
            MY_STATE = ""
            finish()
        }
    }

}





