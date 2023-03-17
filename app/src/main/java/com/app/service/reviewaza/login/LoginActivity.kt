package com.app.service.reviewaza.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.app.service.reviewaza.*
import com.app.service.reviewaza.call.Key.Companion.DB_URL
import com.app.service.reviewaza.call.Key.Companion.DB_USERS
import com.app.service.reviewaza.databinding.ActivityLoginBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
import com.kakao.sdk.user.model.User

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private var auth: FirebaseAuth? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var GOOGLE_LOGIN_CODE = 9001
    private var facebookCallbackManager: CallbackManager? = null
    private lateinit var pendingUser: User

    private val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if(error != null) {
            // 로그인 실패
            showErrorToast()
            error.printStackTrace()

        } else if(token != null) {
            // 로그인 성공
            getKakaoAccountInfo()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KakaoSdk.init(this, getString(R.string.KAKAO_NATIVE_KEY))

        val REQUEST_ID_TOKEN = "${getString(R.string.REQUEST_ID_TOKEN)}"

        auth = FirebaseAuth.getInstance()
        facebookCallbackManager = CallbackManager.Factory.create()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 회원가입 버튼
        binding.loginSignupBtn.setOnClickListener {
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼
        binding.loginLoginBtn.setOnClickListener {
            val email = loginEmail.text.toString()
            val password = login_password.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일 또는 패스워드가 입력되지 않았습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signinEmail(email, password)
        }

        // 구글 로그인 버튼
        binding.googleLoginBtn.setOnClickListener {
            googleLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(REQUEST_ID_TOKEN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 페이스북 로그인 버튼
        binding.facebookLoginBtn.setOnClickListener {
            facebookLogin()
        }

        // 카카오 로그인 버튼
        binding.kakaoLoginBtn.setOnClickListener {
            kakaoLogin()
        }

    }

    //로그아웃하지 않을 시 자동 로그인 , 회원가입시 바로 로그인 됨
    public override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    // 이메일 로그인
    fun signinEmail(email: String, password: String) {

        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                val currentUser = Firebase.auth.currentUser
                if (task.isSuccessful && currentUser != null) {
                    // Login, 아이디와 패스워드가 맞았을 때
                    val userId = currentUser.uid
                    LOGIN_SET = "Email"

                    Firebase.messaging.token.addOnCompleteListener {
                        val token = it.result

                        val user = mutableMapOf<String, Any>()
                        user["userId"] = userId
                        user["username"] = email
                        user["fcmToken"] = token!!

                        Firebase.database(DB_URL).reference.child(DB_USERS).child(userId)
                            .updateChildren(user)
                    }
                    moveMainPage(task.result?.user)
                } else {
                    // Show the error message, 아이디와 패스워드가 틀렸을 때
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    // 구글 로그인 함수
    private fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        if (signInIntent != null) {
            startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
            LOGIN_SET = "Google"
            MY_STATE = googleSignInClient.toString()
            finish()
        }
    }

    // 페이스북 로그인 함수
    private fun facebookLogin() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))

        LoginManager.getInstance()
            .registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    // 로그인 성공시
                    handleFacebookAccessToken(result?.accessToken)
                    // 파이어베이스로 로그인 데이터를 넘겨줌
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }
            })
    }

    fun handleFacebookAccessToken(token: AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)

        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 아이디, 비밀번호 맞을 때
                    moveMainPage(task.result?.user)
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                } else {
                    // 틀렸을 때
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun kakaoLogin() {

        if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            // 카카오톡 로그인
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->

                if(error != null) {
                    // 카카오톡 로그인 실패
                    if(error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = kakaoCallback)
                } else if(token != null) {
                    // 로그인 성공
                    if(Firebase.auth.currentUser == null) {
                        getKakaoAccountInfo()
                    } else {
                        //navigateToMapActivity()
                    }
                }
            }
        } else {
            // 카카오계정 로그인
            UserApiClient.instance.loginWithKakaoAccount(this, callback = kakaoCallback)
        }
    }

    private fun getKakaoAccountInfo() {
        UserApiClient.instance.me { user, error ->
            if(error != null) {
                showErrorToast()
                error.printStackTrace()
            } else if(user != null) {
                // 사용자 정보 요청 성공
                Log.e("LoginActivity",
                    "user: 회원번호 : ${user.id} / 이메일: ${user.kakaoAccount?.email} / 닉네임: ${user.kakaoAccount?.profile?.nickname} / 프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")
                checkKakaoUserData(user)
            }
        }
    }

    private fun checkKakaoUserData(user: User) {
        val kakaoEmail = user.kakaoAccount?.email.orEmpty()

        if(kakaoEmail.isNullOrEmpty()) {
            // 추가로 이메일 받는 과정
            pendingUser = user
        }

        signingFirebase(user, kakaoEmail)
    }

    private fun signingFirebase(user: User, email: String) {
        val uId = user.id.toString()

        Firebase.auth.createUserWithEmailAndPassword(email, uId).addOnCompleteListener {
            if(it.isSuccessful) {
                updateFirebaseDatabase(user)
            }
        }.addOnFailureListener {
            // 이미 가입된 계정
            if(it is FirebaseAuthUserCollisionException) {
                Firebase.auth.signInWithEmailAndPassword(email, uId).addOnCompleteListener { result ->
                    if(result.isSuccessful) {
                        updateFirebaseDatabase(user)
                    } else {
                        showErrorToast()
                    }
                }.addOnFailureListener { error ->
                    error.printStackTrace()
                    showErrorToast()
                }
            } else {
                showErrorToast()
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 아이디, 비밀번호 맞을 때
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    moveMainPage(task.result?.user)
                } else {
                    // 틀렸을 때
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookCallbackManager?.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)!!
            // 구글API가 넘겨주는 값 받아옴

            if (result.isSuccess) {
                var accout = result.signInAccount
                firebaseAuthWithGoogle(accout)
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }

        }
    }

    // 유저정보 넘겨주고 메인 액티비티 호출
    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            MY_STATE = user.toString()
            finish()
        }
    }

    private fun updateFirebaseDatabase(user: User) {
        val uid = Firebase.auth.currentUser?.uid.orEmpty()

        val personMap = mutableMapOf<String, Any>()
        personMap["userId"] = uid
        personMap["username"] = user.kakaoAccount?.profile?.nickname.orEmpty()
        personMap["userImage"] = user.kakaoAccount?.profile?.thumbnailImageUrl.orEmpty()
        Firebase.database(DB_URL).reference.child(DB_USERS).child(uid)
            .updateChildren(personMap)

        Firebase.messaging.token.addOnCompleteListener {
            val token = it.result
            val personMap = mutableMapOf<String, Any>()
            personMap["userId"] = uid
            personMap["username"] = user.kakaoAccount?.profile?.nickname.orEmpty()
            personMap["userImage"] = user.kakaoAccount?.profile?.thumbnailImageUrl.orEmpty()
            personMap["fcmToken"] = token!!

            Firebase.database(DB_URL).reference.child(DB_USERS).child(uid)
                .updateChildren(personMap)
        }
        moveMainPage(auth?.currentUser)
    }

    private fun showErrorToast() {
        Toast.makeText(this, "사용자 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
    }

}