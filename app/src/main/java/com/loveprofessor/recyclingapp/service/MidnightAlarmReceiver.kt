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
        Thread {
            Log.d("jwbaek", "1. onReceive 호출됨")
            val prefs: SharedPreferences = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
            val stepCount = prefs.getInt("step_count", 0)
            val todayStepCount = prefs.getInt("today_step_count", 0)

            Log.d("jwbaek", "2. stepCount : $stepCount , todayStepCount : $todayStepCount")

            // Firestore에 오늘의 걸음 수를 저장
            saveStepCount(context, todayStepCount)

            // 자정이 되면 현재 누적 걸음수를 last_step_count로 저장
            resetStepCount(context)
        }.start()
    }

    private fun saveStepCount(context: Context, todayStepCount: Int) {
        val db = FirebaseFirestore.getInstance()
        val reportDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        Log.d("jwbaek", "3. fireStore에 저장하는 로직에 탔음.")

        val reportData = hashMapOf(
            "userUid" to MyApplication.uId,
            "report_date" to reportDate,
            "report_step_data" to todayStepCount,
            "report_carbon_data" to calculateCarbon(todayStepCount)
        )

        db.collection("steps_report")
            .add(reportData)
            .addOnSuccessListener {
                Log.d("MidnightReceiver", "걸음 수 데이터 저장 성공: $todayStepCount")
                Toast.makeText(context, "오늘 데이터를 저장했습니다.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.e("MidnightReceiver", "걸음 수 데이터 저장 실패", e)
                Toast.makeText(context, "오늘 데이터에 실패했습니다. ${e.message}.", Toast.LENGTH_LONG).show()
            }
    }

    private fun resetStepCount(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        val currentStepCount = prefs.getInt("step_count", 0)

        prefs.edit()
            .putInt("last_step_count", currentStepCount)  // 현재 누적 걸음수를 저장
            .putInt("today_step_count", 0)  // 오늘의 걸음수 초기화
            .apply()
    }

    companion object {
        fun calculateCarbon(stepCount: Int): Float {
            val METER_PER_STEP = 0.7
            val KM_PER_CAR = 166.0

            val totalStep = stepCount * METER_PER_STEP
            val totalStepKm = totalStep / 1000
            val carbonData = totalStepKm * KM_PER_CAR
            return carbonData.toFloat()
        }
    }
}

