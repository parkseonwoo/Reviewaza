package com.app.service.reviewaza.login

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private var auth: FirebaseAuth? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var GOOGLE_LOGIN_CODE = 9001
    private var facebookCallbackManager: CallbackManager? = null
    lateinit var kakaoCallback: (OAuthToken?, Throwable?) -> Unit

    private var myUserId = Firebase.auth.currentUser?.uid ?: null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KakaoSdk.init(this, "{KAKAO_NATIVE_KEY}")

        val REQUEST_ID_TOKEN = "${getString(R.string.REQUEST_ID_TOKEN)}"

        auth = FirebaseAuth.getInstance()
        facebookCallbackManager = CallbackManager.Factory.create()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ???????????? ??????
        binding.loginSignupBtn.setOnClickListener {
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // ????????? ??????
        binding.loginLoginBtn.setOnClickListener {
            val email = loginEmail.text.toString()
            val password = login_password.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "????????? ?????? ??????????????? ???????????? ???????????????", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signinEmail(email, password)
        }

        // ?????? ????????? ??????
        binding.googleLoginBtn.setOnClickListener {
            googleLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(REQUEST_ID_TOKEN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // ???????????? ????????? ??????
        binding.facebookLoginBtn.setOnClickListener {
            facebookLogin()
        }

        // ????????? ????????? ??????
        binding.kakaoLoginBtn.setOnClickListener {

            kakaoLogin()
        }

    }

    //?????????????????? ?????? ??? ?????? ????????? , ??????????????? ?????? ????????? ???
    public override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    // ????????? ?????????
    fun signinEmail(email: String, password: String) {

        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                val currentUser = Firebase.auth.currentUser
                if (task.isSuccessful && currentUser != null) {
                    // Login, ???????????? ??????????????? ????????? ???
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
                    // Show the error message, ???????????? ??????????????? ????????? ???
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    // ?????? ????????? ??????
    private fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        if (signInIntent != null) {
            startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
            LOGIN_SET = "Google"
            MY_STATE = googleSignInClient.toString()
            finish()
        }
    }

    // ???????????? ????????? ??????
    private fun facebookLogin() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))

        LoginManager.getInstance()
            .registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    // ????????? ?????????
                    handleFacebookAccessToken(result?.accessToken)
                    // ????????????????????? ????????? ???????????? ?????????
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }
            })
    }

    fun handleFacebookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)

        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    // ?????????, ???????????? ?????? ???
                    moveMainPage(task.result?.user)
                    Toast.makeText(this,"????????? ??????",Toast.LENGTH_SHORT).show()
                }else{
                    // ????????? ???
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun kakaoLogin() {
        setKaKaoCallback()
        if(myUserId == null) {
            if(LoginClient.instance.isKakaoTalkLoginAvailable(this)){
                LoginClient.instance.loginWithKakaoTalk(this, callback = kakaoCallback)
            }else{
                LoginClient.instance.loginWithKakaoAccount(this, callback = kakaoCallback)
            }
        }
    }

    fun setKaKaoCallback() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "????????? ?????? ???(?????? ??????)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Toast.makeText(this, "???????????? ?????? ???", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Toast.makeText(this, "?????? ????????? ???????????? ?????? ????????? ??? ?????? ??????", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Toast.makeText(this, "?????? ???????????? ??????", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Toast.makeText(this, "???????????? ?????? scope ID", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Toast.makeText(this, "????????? ???????????? ??????(android key hash)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Toast.makeText(this, "?????? ?????? ????????? ??????", Toast.LENGTH_SHORT).show()
                    }
                    else -> { // Unknown
                        Toast.makeText(this, "?????? ??????", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (token != null) {
                Toast.makeText(this, "???????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                LOGIN_SET = "Kakao"
                MY_STATE = token.toString()
                finish()
            }
        }
        if(LoginClient.instance.isKakaoTalkLoginAvailable(this)){
            LoginClient.instance.loginWithKakaoTalk(this, callback = callback)

        }else{
            LoginClient.instance.loginWithKakaoAccount(this, callback = callback)
        }

    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ?????????, ???????????? ?????? ???
                    Toast.makeText(this, "????????? ??????", Toast.LENGTH_SHORT).show()
                    moveMainPage(task.result?.user)
                } else {
                    // ????????? ???
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookCallbackManager?.onActivityResult(requestCode,resultCode,data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)!!
            // ??????API??? ???????????? ??? ?????????

            if (result.isSuccess) {
                var accout = result.signInAccount
                firebaseAuthWithGoogle(accout)
                Toast.makeText(this, "????????? ??????", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "????????? ??????", Toast.LENGTH_SHORT).show()
            }

        }
    }

    // ???????????? ???????????? ?????? ???????????? ??????
    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            MY_STATE = user.toString()
            finish()
        }
    }

companion object {
    const val TAG = "MyLog"
}

}