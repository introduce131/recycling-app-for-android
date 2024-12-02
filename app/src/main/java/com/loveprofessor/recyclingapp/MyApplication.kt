package com.loveprofessor.recyclingapp

import android.app.Application
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
}