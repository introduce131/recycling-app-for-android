package com.loveprofessor.recyclingapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.loveprofessor.recyclingapp.MyApplication
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MidnightAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("jwbaek", "onReceive 호출됨")  // 로그 추가
        // 1. 일단 SharedPreferences에서 오늘의 걸음 수(step_prefs)를 가져오기
        val prefs: SharedPreferences = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        val stepCount = prefs.getInt("step_count", 0)
        val todayStepCount = prefs.getInt("today_step_count", 0)

        // Firestore에 오늘의 걸음 수를 저장
        saveStepCount(context, todayStepCount)

        // SharedPreferences에서 걸음 수 초기화
        resetStepCount(context)
    }

    private fun saveStepCount(context: Context, todayStepCount: Int) {
        val db = FirebaseFirestore.getInstance()
        val reportDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        Log.d("JWBAEK", "이 로직을 제시간안에 타야한다.")

        val reportData = hashMapOf(
            "userUid" to MyApplication.uId,
            "report_date" to reportDate,
            "report_step_data" to todayStepCount,
            "report_carbon_data" to calculateCarbon(todayStepCount) // 탄소 절감량 계산
        )

        db.collection("steps_report")
            .add(reportData)
            .addOnSuccessListener {
                // 데이터 저장 성공 시 로그 출력
                Log.d("MidnightReceiver", "걸음 수 데이터 저장 성공: $todayStepCount")
                Toast.makeText(context, "오늘 데이터를 저장했습니다.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                // 저장 실패 시 에러 로그 출력
                Log.e("MidnightReceiver", "걸음 수 데이터 저장 실패", e)
                Toast.makeText(context, "오늘 데이터에 실패했습니다. ${e.message}.", Toast.LENGTH_LONG).show()
            }
    }

    // 걸음수를 초기화 해야 함.
    private fun resetStepCount(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        val stepCount = prefs.getInt("step_count", 0)

        prefs.edit().putInt("last_step_count", stepCount).apply()   // 여태까지의 누적 걸음수를 last_step_count에 저장함.
    }

    // 탄소 저감량을 계산해주는 메소드 calculateCarbon, 일단 계산은 대강 해놨음
    companion object {
        fun calculateCarbon(stepCount: Int): Float {
            val METER_PER_STEP = 0.7    // 100걸음에 70m, 1보당 70cm
            val KM_PER_CAR = 166.0      // 자동차 1KM 주행시 탄소 배출량 166g

            val totalStep = stepCount * METER_PER_STEP // 총 걸음수 = 걸음수 * 보폭(70cm)
            val totalStepKm = totalStep / 1000  // 총 걸음수(미터)를 킬로미터로 변환

            // 탄소 저감량을 계산 (자동차로 주행했을 경우 배출되는 CO2를 기준으로 했음)
            val carbonData = totalStepKm * KM_PER_CAR
            return carbonData.toFloat()
        }
    }
}
