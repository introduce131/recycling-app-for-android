package com.loveprofessor.recyclingapp.report

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.loveprofessor.recyclingapp.databinding.FragmentReportHomeBinding

class ReportHomeFragment : Fragment() {

    private lateinit var binding: FragmentReportHomeBinding
    private lateinit var stepCountTextView: TextView

    // BroadcastReceiver 등록하여 걸음 수 실시간으로 받기
    private val stepCountReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.loveprofessor.walksensor.STEP_COUNT_UPDATED") {
                val stepCount = intent.getIntExtra("step_count", 0)
                updateStepCount(stepCount) // 걸음 수 업데이트
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReportHomeBinding.inflate(inflater, container, false)

        stepCountTextView = binding.stepCountTextView

        // SharedPreferences에서 걸음 수 가져오기 (초기값 설정)
        val prefs: SharedPreferences = requireContext().getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        val stepCount = prefs.getInt("step_count", 0)

        // 초기 걸음 수 표시
        stepCountTextView.text = "걸음 수 : $stepCount"

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // BroadcastReceiver 등록
        val filter = IntentFilter("com.loveprofessor.walksensor.STEP_COUNT_UPDATED")
        requireContext().registerReceiver(stepCountReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        // BroadcastReceiver 해제
        requireContext().unregisterReceiver(stepCountReceiver)
    }

    // MainActivity로부터 걸음 수를 업데이트 받는 메서드
    fun updateStepCount(stepCount: Int) {
        // 걸음 수를 SharedPreferences에 저장
        val prefs: SharedPreferences = requireContext().getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("step_count", stepCount).apply()

        // UI 업데이트
        stepCountTextView.text = "걸음 수 : $stepCount"
    }
}
