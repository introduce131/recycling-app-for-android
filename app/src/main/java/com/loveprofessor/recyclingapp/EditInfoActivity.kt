package com.loveprofessor.recyclingapp

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.loveprofessor.recyclingapp.camera.ClassifyResultData
import com.loveprofessor.recyclingapp.databinding.ActivityEditInfoBinding
import com.loveprofessor.recyclingapp.faq.FaqResultData

class EditInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditInfoBinding
    private lateinit var editNickName: TextView
    private lateinit var checkBMon: CheckBox
    private lateinit var checkBTue: CheckBox
    private lateinit var checkBWed: CheckBox
    private lateinit var checkBThu: CheckBox
    private lateinit var checkBFri: CheckBox
    private lateinit var checkBSat: CheckBox
    private lateinit var checkBSun: CheckBox
    private lateinit var spinnerZone: Spinner
    private lateinit var buttonComp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        checkBMon = binding.checkBMon
        checkBTue = binding.checkBTue
        checkBWed = binding.checkBWed
        checkBThu = binding.checkBThu
        checkBFri = binding.checkBFri
        checkBSat = binding.checkBSat
        checkBSun = binding.checkBSun
        spinnerZone = binding.spinnerZone
        editNickName = binding.editNickName
        buttonComp = binding.buttonComplete

        editNickName.text = MyApplication.userNickname

        // 문자열을 처리하는 메서드
        checkDays(MyApplication.userGarbageday)

        // 서울 지역구 목록 배열 설정
        val zoneItems = arrayOf(
            "선택하세요", "강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구",
            "금천구", "노원구","도봉구", "동대문구", "동작구", "마포구", "서대문구", "서초구",
            "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구",
            "중구", "중랑구"
        )

        // ArrayAdapter 생성
        val myAdapter = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, zoneItems)

        // 프래그먼트의 spinner에 Adapter 설정
        spinnerZone.adapter = myAdapter

        val position = zoneItems.indexOf(MyApplication.userZone)
        if (position != -1) spinnerZone.setSelection(position)

        buttonComp.setOnClickListener {
            updateUser()
        }

        binding.root.setOnClickListener {
            hideKeyboard()
            false
        }
    }

    private fun updateUser() {
        // 선택된 요일들 리스트를 만들려고 함
        val selectDays = mutableListOf<String>()

        if (checkBMon.isChecked) selectDays.add("월요일")
        if (checkBTue.isChecked) selectDays.add("화요일")
        if (checkBWed.isChecked) selectDays.add("수요일")
        if (checkBThu.isChecked) selectDays.add("목요일")
        if (checkBFri.isChecked) selectDays.add("금요일")
        if (checkBSat.isChecked) selectDays.add("토요일")
        if (checkBSun.isChecked) selectDays.add("일요일")

        // selectedDays 리스트를 ", "로 구분된 문자열로 합친다.
        val selectedDaysString = selectDays.joinToString(", ")  // 날짜
        val nickName = editNickName.text.toString().trim()  // 닉네임
        val zone = spinnerZone.selectedItem.toString()  // 지역구
        val userUid = MyApplication.uId

        // Firestore에 저장할 데이터 구조
        val userUpdates = hashMapOf<String, Any>(
            "userNickname" to nickName,
            "userGarbageday" to selectedDaysString,
            "userZone" to zone
        )

        val collectionRef = FirebaseFirestore.getInstance().collection("users")
        collectionRef.whereEqualTo("userUid", userUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(querySnapshot.isEmpty) {
                    Log.d("jwbaek", "uid : $userUid")
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setContentText("유저를 찾을 수 없습니다!")
                        .setConfirmText("확인")
                        .setConfirmClickListener { dialog ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    val document = querySnapshot.documents[0]
                    collectionRef.document(document.id)
                        .update(userUpdates)
                        .addOnSuccessListener {
                            // 1. Firestore 업데이트 성공 후 MyApplication에 먼저 값을 저장하고,
                            MyApplication.userNickname = nickName
                            MyApplication.userZone = zone
                            MyApplication.userGarbageday = selectedDaysString

                            // 2. SweetAlertDialog로 성공 메시지를 표시한다.
                            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setContentText("정상적으로 업데이트 되었습니다!")
                                .setConfirmText("확인")
                                .setConfirmClickListener { dialog ->
                                    dialog.dismiss()
                                    // onCreate()를 다시 호출해야 되서, 그냥 MainActivity를 새로 시작하는 방향으로 하는게 나을 듯 하다.
                                    val intent = Intent(this, MainActivity::class.java)

                                    // 새로운 태스크에서 MainActivity를 시작하도록 설정
                                    /* 출처 : https://blog.naver.com/estern/220012629594 */
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish() // 그리고 현재 화면을 종료시킴.
                                }
                                .show()
                        }
                        .addOnFailureListener { e ->
                            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                .setContentText("업데이트에 실패했습니다.")
                                .setConfirmText("확인")
                                .setConfirmClickListener { dialog ->
                                    dialog.dismiss()
                                    Log.d("EditInfo", "${e.message}")
                                }
                                .show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "알 수 없는 오류 : ${e.message}", Toast.LENGTH_LONG).show()
            }

    }

    private fun checkDays(daysString: String) {
        // 쉼표로 구분된 요일들을 리스트로 분리
        val daysList = daysString.split(",").map { it.trim() }

        // 각 체크박스를 해당 요일에 맞게 체크
        for (day in daysList) {
            when (day) {
                "월요일" -> checkBMon.isChecked = true
                "화요일" -> checkBTue.isChecked = true
                "수요일" -> checkBWed.isChecked = true
                "목요일" -> checkBThu.isChecked = true
                "금요일" -> checkBFri.isChecked = true
                "토요일" -> checkBSat.isChecked = true
                "일요일" -> checkBSun.isChecked = true
            }
        }
    }

    private fun hideKeyboard() {
        this.currentFocus?.let { focusView ->
            val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputManager?.hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}