package com.loveprofessor.recyclingapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.loveprofessor.recyclingapp.databinding.ActivityLoginBinding

/* LoginActivity.kt */

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val REQ_ONE_TAP = 2                     // Google One Tap SignIn에 대한 요청 코드??
    private var showOneTapUI = true                 // OneTapUI 표시 여부, 기본값은 true
    private lateinit var oneTapClient: SignInClient         // SignInClient 객체
    private lateinit var signInRequest: BeginSignInRequest  // 로그인 요청 객체 SignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인 상태 확인 (SharedPreferences에 저장된 uId를 기준으로 확인하면 됨)
        val prefs: SharedPreferences = this.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val uId = prefs.getString("uId", null)

        Log.d("jwbaek", "uid : $uId")

        if(uId != null) {
            // uId가 존재하면 이미 로그인된 상태니까, 바로 MainActivity로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()    // LoginActivity는 종료시킴
        } else {
            // OneTapSignIn 클라이언트 초기화
            oneTapClient = Identity.getSignInClient(this)

            // Google 로그인 요청 설정(Token 요청 설정)
            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true) // 토큰을 요청
                        .setServerClientId(getString(R.string.client_id))  // 웹 클라이언트 ID
                        .setFilterByAuthorizedAccounts(false)   // false로 해야 인증되지 않은 계정도 등록할 수 있음.
                        .build()
                )
                .build()

            // 구글 로그인 버튼 클릭 이벤트
            binding.googleLoginBtn.setOnClickListener {

                // OneTapSignIn의 시작
                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(this) { result ->
                        try {
                            startIntentSenderForResult(
                                result.pendingIntent.intentSender,
                                REQ_ONE_TAP,
                                null, 0, 0, 0, null
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e("Google", "One Tap UI 실행 실패 : ${e.localizedMessage}")
                        }
                    }
                    .addOnFailureListener(this) { e ->
                        Log.e("Google", "One Tap Sign-In 실패: ${e.localizedMessage}")
                    }
            }
        }
    }

    // 구글 인증의 결과는 onActivityResult, 이곳 에서 처리함
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    try {
                        // 로그인 결과로 받은 자격증명들
                        val credential = oneTapClient.getSignInCredentialFromIntent(data)
                        val idToken = credential.googleIdToken  // googleId 토큰
                        val username = credential.id            // 사용자 ID
                        val password = credential.password      // 비밀번호, 쓸일이 없네

                        // ID Token의 유무에 따라 파이어베이스 인증도 처리함
                        if (idToken != null) {
                            Log.d("Google", "ID Token: $idToken")
                            Log.d("Email", "Email is $username")
                            firebaseAuthWithGoogle(idToken)  // Firebase 인증 진행
                        } else {
                            Log.d("Google", "No ID Token received.")
                        }
                    } catch (e: ApiException) {
                        when (e.statusCode) {
                            CommonStatusCodes.CANCELED -> {
                                Log.d("Google", "One Tap dialog was closed.")
                                showOneTapUI = false    // 유저가 원탭 UI를 닫았으면 false로 화면에 표시를 하지 않음
                            }
                            else -> {
                                Log.e("Google", "Google SignIn failed: ${e.localizedMessage}")
                            }
                        }
                    }
                }
                Activity.RESULT_CANCELED -> { // 여기는 사용자가 로그인을 취소한 경우
                    Log.d("Google", "User canceled the SignIn.")
                }
            }
        }
    }

    // firebase 인증, Authentication
    private fun firebaseAuthWithGoogle(idToken: String) {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()         // firebase의 firestore 인스턴스 초기화
        val credential = GoogleAuthProvider.getCredential(idToken, null)    // id Token으로 Google 인증 자격증명을 생성
        FirebaseAuth.getInstance().signInWithCredential(credential)         // Google 인증 자격증명으로 Firebase 인증 진행
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Firebase 인증 성공 후, Firebase의 현재 유저 정보 받아오기
                    val user = FirebaseAuth.getInstance().currentUser

                    // MyApplication 클래스에 사용자 정보 저장
                    MyApplication.uId = user?.uid.toString()
                    MyApplication.userEmail = user?.email.toString()
                    MyApplication.userName = user?.displayName.toString()

                    // Firestore에서 유저 정보 가져오기
                    db.collection("users").document(MyApplication.uId)
                        .get()
                        .addOnSuccessListener { document ->
                            val intent: Intent
                            if (document.exists()) { // 문서가 존재하면 로그인된 사용자
                                val userData = document.toObject(UserData::class.java)
                                if (userData != null) {
                                    // Firestore에서 가져온 사용자 정보로 MyApplication 업데이트
                                    MyApplication.userNickname = userData.userNickname
                                    MyApplication.userZone = userData.userZone
                                    MyApplication.userGarbageday = userData.userGarbageday

                                    // MainActivity로 이동
                                    intent = Intent(this, MainActivity::class.java)
                                } else {
                                    // Firestore에 데이터는 있으나 정보가 부족하면 InputActivity로 이동
                                    intent = Intent(this, InputActivity::class.java)
                                }
                            } else { // Firestore에 문서가 없으면 회원가입 화면으로 이동
                                intent = Intent(this, InputActivity::class.java)
                            }
                            // 새로운 Activity로 이동
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error getting user data: ${e.localizedMessage}")
                            // Firestore 조회 실패 시 InputActivity로 이동
                            val intent = Intent(this, InputActivity::class.java)
                            startActivity(intent)
                        }
                } else {
                    // Firebase 인증 실패
                    Log.e("Google", "Firebase authentication failed")
                }
            }
    }

}