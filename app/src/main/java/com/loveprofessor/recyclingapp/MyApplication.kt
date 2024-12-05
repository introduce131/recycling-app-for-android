package com.loveprofessor.recyclingapp

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class MyApplication:Application() { // 신기하다. Manifest.xml의 Application에 name을 붙여서 상속받음.
    companion object {
        lateinit var uId:String             // 얘를 user(Collection)의 문서 id로 쓰면 식별자로 적당할 듯
        lateinit var userEmail:String       // 구글 email 주소
        lateinit var userName:String        // 구글계정 이름
        lateinit var userNickname:String    // 앱에서 지정한 닉네임
        lateinit var userZone:String        // 앱에서 지정한 사는 지역
        lateinit var userGarbageday:String   // 앱에서 지정한 쓰레기 버리는 요일 (구분자 ",")

        // 유저 정보 초기화 static method
        fun userInfoReset() {
            this.uId = ""
            this.userEmail = ""
            this.userName = ""
            this.userNickname = ""
            this.userZone = ""
            this.userGarbageday = ""
        }
    }

    override fun onCreate() {
        super.onCreate()

        // SharedPreferences 초기화 코드를 추가 (2024-12-06)
        val prefs = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("isFirstRun", true)

        Log.d("isFirst", "$isFirstRun")

        if (isFirstRun) {
            // 앱이 처음 설치되었을 때 초기화 작업을 해줘야함, 사실 이렇게까지 해야하나 싶기도한데 이게 맞는 듯?..
            prefs.edit().clear().apply()  // SharedPreferences 초기화
            prefs.edit().putBoolean("isFirstRun", false).apply()  // 이후 앱 실행에서는 초기화하지 않음
        }
    }
}