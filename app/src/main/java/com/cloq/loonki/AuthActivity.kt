package com.cloq.loonki

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloq.loonki.dialog.LoadingDialog
import com.example.awesomedialog.*
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.common.KakaoSdk
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

private const val RC_SIGN_IN = 9001
private const val TAG = "MainActivity"
private var googleSignInClient: GoogleSignInClient? = null

class AuthActivity : AppCompatActivity() {
    private lateinit var ld: LoadingDialog
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        KakaoSdk.init(this, getString(R.string.kakao_app_key))

        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result?.token
                val msg = token.toString()
                Log.d(TAG, msg)
            })

        login_button.setOnClickListener() {
            ld = LoadingDialog(this)
            ld.start()
            loginWithEmail()
        }

        google_login.setOnClickListener() {
            ld = LoadingDialog(this)
            ld.start()
            loginWithGoogle()
        }

        facebook_login.setOnClickListener {
            ld = LoadingDialog(this)
            ld.start()
            facebookLogin()
        }

        kakao_login.setOnClickListener {
            Toast.makeText(this, "개발중입니다..", Toast.LENGTH_SHORT).show()
            //kakaoLogin()
        }

        register_button.setOnClickListener() {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun checkAccountAvailable(user: String, type: String, email: String) {
        fs.collection("users").document(user).get().addOnSuccessListener {
            when {
                (it.get("valid") == true) -> afterLogin(user)
                else -> {
                    AwesomeDialog.build(this)
                        .title("회원가입을 축하합니다!", titleColor = Color.parseColor("#EDD7C3"))
                        .body(
                            "서비스를 시작하기 전 몇 가지 정보를 더 입력해야 합니다.",
                            color = Color.parseColor("#EDD7C3")
                        )
                        .background(R.drawable.dialog_bg)
                        .onPositive(
                            "확인",
                            buttonBackgroundColor = R.drawable.dialog_btn_bg,
                            textColor = Color.parseColor("#1A253F")
                        ) {
                            val intent = Intent(this, SocialRegisterActivity::class.java)
                            intent.putExtra("UID", user)
                            intent.putExtra("TYPE", type)
                            intent.putExtra("EMAIL", email)
                            startActivity(intent)
                        }
                }
            }
        }
    }

    private fun afterLogin(uid: String) {
        val storageToken: String = App.prefs.getPref("FCM_TOKEN", "no Token")
        App.prefs.setPref("UID", uid)

        if (storageToken == "no Token") {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "getInstanceId failed", task.exception)
                        Toast.makeText(this, "토큰을 받을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        return@OnCompleteListener
                    }
                    val token = task.result?.token
                    if (token != null) {
                        App.prefs.setPref("FCM_TOKEN", token)
                        setTokenInfo(token, uid)
                    }
                }).addOnFailureListener { e ->
                    Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
                }
        } else {
            setTokenInfo(storageToken, uid)
        }
    }

    private fun setTokenInfo(storageToken: String, uid: String) {
        fs.collection("tokens").document(storageToken).set(
            hashMapOf(
                "uid" to uid
            ) as Map<String, String>
        )
        fs.collection("users").document(uid).update(
            hashMapOf(
                "token" to storageToken
            ) as Map<String, String>
        )
        intentHome()
    }

    private fun intentHome() {
        val intent = Intent(this, MainActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    }

    private fun loginWithGoogle() {
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.w("GOOGLE", "firebaseAuthWithGoogle: ${account.id}")
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("GOOGLE", "Google 로그인 실패", e)
            }
        }
    }

    private fun kakaoLogin() {
        if (LoginClient.instance.isKakaoTalkLoginAvailable(this)) {
            LoginClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e("KAKAO", "KAKAO 로그인 실패", error)
                } else if (token != null) {
                    Log.d("KAKAO", "KAKAO 로그인 성공 USER: ${token.accessToken}")
                }
            }
        } else {
            LoginClient.instance.loginWithKakaoAccount(this) { token, error ->
                if (error != null) {
                    Log.e("KAKAO", "KAKAO 로그인 실패", error)
                } else if (token != null) {
                    Log.d("KAKAO", "KAKAO 로그인 성공 USER: ${token.accessToken}")
                }
            }
        }
    }

    private fun facebookLogin() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult?) {

                val request = GraphRequest.newMeRequest(
                    result?.accessToken
                ) { `object`, response ->
                    Log.v(TAG, response.toString());
                    val email: String = `object`.getString("email")
                    firebaseAuthWithFacebook(result?.accessToken, email)
                }

                val parameters = Bundle()
                parameters.putString(
                    "fields",
                    "email"
                )
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                ld.dismiss()
            }

            override fun onError(error: FacebookException?) {
                ld.dismiss()
            }
        })
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid.toString()
                    val email = user?.email.toString()
                    Log.d("GOOGLE", "GOOGLE 로그인 성공 USER: ${user?.email}")
                    if (user != null) {
                        ld.dismiss()
                        checkAccountAvailable(uid, "Google", email)
                    } else {
                        ld.dismiss()
                        Toast.makeText(this, "로그인에 문제가 생겼습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w("GOOGLE", "GOOGLE 로그인 오류")
                    fs.collection("users").whereEqualTo("email", account.email).get()
                        .addOnSuccessListener { qs ->
                            val msg = when (qs.documents[0].get("type")) {
                                "Email" -> "해당 이메일로 가입된 계정이 있습니다."
                                "Facebook" -> "해당 계정은 Facebook계정과 연동중입니다."
                                "Kakao" -> "해당 계정은 Kakao계정과 연동중입니다."
                                else -> "이미 가입된 계정입니다."
                            }
                            if (qs.documents.size == 1) {
                                ld.dismiss()
                                AwesomeDialog.build(this)
                                    .title("로그인 오류", titleColor = Color.parseColor("#EDD7C3"))
                                    .body(msg, color = Color.parseColor("#EDD7C3"))
                                    .background(R.drawable.dialog_bg)
                                    .onPositive(
                                        "확인",
                                        buttonBackgroundColor = R.drawable.dialog_btn_bg,
                                        textColor = Color.parseColor("#1A253F")
                                    ) {

                                    }
                            } else {
                                ld.dismiss()
                                Toast.makeText(this, "로그인에 문제가 생겼습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                }
            }
    }

    private fun firebaseAuthWithFacebook(token: AccessToken?, email: String) {
        if (token != null) {
            val credential = FacebookAuthProvider.getCredential(token.token)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid.toString()
                        Log.d("FACEBOOK", "FACEBOOK 로그인 성공 USER: ${user?.email}")
                        if (user != null) {
                            ld.dismiss()
                            checkAccountAvailable(uid, "Facebook", email)
                        } else {
                            ld.dismiss()
                            Toast.makeText(this, "로그인에 문제가 생겼습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.w("FACEBOOK", "FACEBOOK 로그인 오류", task.exception)
                        fs.collection("users").whereEqualTo("email", email).get()
                            .addOnSuccessListener { qs ->
                                val msg = when (qs.documents[0].get("type")) {
                                    "Email" -> "해당 이메일로 가입된 계정이 있습니다."
                                    "Google" -> "해당 계정은 Google계정과 연동중입니다."
                                    "Kakao" -> "해당 계정은 Kakao계정과 연동중입니다."
                                    else -> "이미 가입된 계정입니다."
                                }
                                if (qs.documents.size == 1) {
                                    ld.dismiss()
                                    AwesomeDialog.build(this)
                                        .title("로그인 오류", titleColor = Color.parseColor("#EDD7C3"))
                                        .body(msg, color = Color.parseColor("#EDD7C3"))
                                        .background(R.drawable.dialog_bg)
                                        .onPositive(
                                            "확인",
                                            buttonBackgroundColor = R.drawable.dialog_btn_bg,
                                            textColor = Color.parseColor("#1A253F")
                                        ) {

                                        }
                                } else {
                                    ld.dismiss()
                                    Toast.makeText(this, "로그인에 문제가 생겼습니다.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                    }
                }
        }
    }

    private fun loginWithEmail() {
        if (!checkEmail(login_email.text.toString())) {
            Toast.makeText(this.applicationContext, "이메일 형식을 확인해주세요..", Toast.LENGTH_SHORT).show()
        } else if (login_password.text.isBlank()) {
            Toast.makeText(this.applicationContext, "패스워드를 입력해주세요..", Toast.LENGTH_SHORT).show()
        } else {
            auth.signInWithEmailAndPassword(
                login_email.text.toString(),
                login_password.text.toString()
            )
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        ld.dismiss()
                        val user = auth.currentUser
                        val uid = auth.currentUser?.uid.toString()
                        afterLogin(uid)
                        Log.d("EMAIL", "EMAIL 로그인 성공 USER: ${user?.email}")
                    } else {
                        ld.dismiss()
                        Toast.makeText(this.applicationContext, "로그인에 실패했습니다.", Toast.LENGTH_SHORT)
                            .show()
                        Log.w("EMAIL", "EMAIL 로그인 오류")
                    }
                }
        }
    }

    private fun checkEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}


