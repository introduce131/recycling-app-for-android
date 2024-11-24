package com.loveprofessor.recyclingapp.report

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import com.loveprofessor.recyclingapp.CustomMarkerView
import com.loveprofessor.recyclingapp.MyApplication
import com.loveprofessor.recyclingapp.R
import com.loveprofessor.recyclingapp.databinding.FragmentReportHomeBinding
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

class ReportHomeFragment : Fragment() {

    private lateinit var binding: FragmentReportHomeBinding
    private lateinit var stepCountTextView: TextView
    private lateinit var barChart: BarChart
    private lateinit var dateText: TextView
    private lateinit var buttonPrevious: ImageButton
    private lateinit var buttonNext: ImageButton
    private var baseDate: LocalDate = LocalDate.now()  // baseDate를 클래스 레벨 변수로 이동

    private val stepCountReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.loveprofessor.recyclingapp.STEP_COUNT_UPDATED") {
                val stepCount = intent.getIntExtra("step_count", 0)
                updateStepCount(stepCount)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportHomeBinding.inflate(inflater, container, false)

        stepCountTextView = binding.stepCountTextView
        buttonPrevious = binding.imageButtonPrevious
        buttonNext = binding.imageButtonNext
        dateText = binding.dateText
        barChart = binding.barChart

        // 마커 설정
        val markerView = CustomMarkerView(requireContext(), R.layout.custom_marker)
        barChart.marker = markerView

        // 차트 기본 설정
        setupChartDefaults()

        // 초기 날짜 범위 설정
        val list = getCurrentWeekDays(baseDate)
        updateDateRange(list)

        // 초기 차트 데이터 로드
        setMPChart(list)  // 여기에 추가

        // 이전 버튼 클릭 리스너
        buttonPrevious.setOnClickListener {
            baseDate = baseDate.minusDays(7)
            val prevList = getCurrentWeekDays(baseDate)
            updateDateRange(prevList)
            setMPChart(prevList)
        }

        // 다음 버튼 클릭 리스너
        buttonNext.setOnClickListener {
            baseDate = baseDate.plusDays(7)
            val nextList = getCurrentWeekDays(baseDate)
            updateDateRange(nextList)
            setMPChart(nextList)
        }

        // 초기 걸음 수 설정
        val prefs: SharedPreferences = requireContext().getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        val stepCount = prefs.getInt("step_count", 0)
        stepCountTextView.text = "$stepCount"

        return binding.root
    }

    private fun setupChartDefaults() {
        with(barChart) {
            setDragEnabled(false)
            setScaleEnabled(false)
            description.isEnabled = false
            setFitBars(true)
            setExtraOffsets(0f, 0f, 0f, 10f)
        }
    }

    private fun updateDateRange(list: List<String>) {
        val firstDay = list.first()
        val lastDay = list.last()
        dateText.text = "$firstDay ~ $lastDay"
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("com.loveprofessor.recyclingapp.STEP_COUNT_UPDATED")
        requireContext().registerReceiver(stepCountReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(stepCountReceiver)
    }

    fun updateStepCount(stepCount: Int) {
        val prefs: SharedPreferences = requireContext().getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("step_count", stepCount).apply()
        stepCountTextView.text = "$stepCount"
    }

    private fun getCurrentWeekDays(date: LocalDate): MutableList<String> {
        val weekDays = mutableListOf<String>()
        val monday = date.minusDays((date.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        for(i in 0..6) {
            val currentDate = monday.plusDays(i.toLong()).format(formatter)
            weekDays.add(currentDate)
        }

        return weekDays
    }

    private fun setMPChart(list: MutableList<String>) {
        val steps = mutableListOf<BarEntry>()
        val carbons = mutableListOf<BarEntry>()

        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("steps_report")

        collectionRef
            .whereEqualTo("userUid", MyApplication.uId)
            .whereIn("report_date", list)
            .orderBy("report_date", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // 먼저 모든 날짜에 대해 기본값 0으로 초기화
                for (i in list.indices) {
                    steps.add(BarEntry(i.toFloat(), 0f))
                    carbons.add(BarEntry(i.toFloat(), 0f))
                }

                // Firestore 데이터로 업데이트
                for (document in querySnapshot) {
                    val date = document.getString("report_date") ?: continue
                    val stepData = document.getLong("report_step_data") ?: 0
                    val carbonData = document.getLong("report_carbon_data") ?: 0

                    val index = list.indexOf(date)
                    if (index != -1) {
                        steps[index] = BarEntry(index.toFloat(), stepData.toFloat())
                        carbons[index] = BarEntry(index.toFloat(), carbonData.toFloat())
                    }
                }

                // 데이터 정렬
                steps.sortBy { it.x }
                carbons.sortBy { it.x }

                // 차트 업데이트
                setChartBar(steps, carbons)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "데이터를 가져오는 중 에러가 발생했습니다.", Toast.LENGTH_LONG).show()
                Log.e("SetChartException", exception.message, exception)
            }
    }

    private fun setChartBar(steps: List<BarEntry>, carbons: List<BarEntry>) {
        val stepsDataSet = BarDataSet(steps, "걸음수").apply {
            color = Color.parseColor("#9CD690")
            valueTextColor = Color.BLACK
            setDrawValues(false)
        }

        val carbonReductionDataSet = BarDataSet(carbons, "탄소 절감량").apply {
            color = Color.parseColor("#9898EC")
            valueTextColor = Color.BLACK
            setDrawValues(false)
        }

        val barData = BarData(stepsDataSet, carbonReductionDataSet).apply {
            barWidth = 0.3f
        }

        // 차트 하단에 추가 여백
        barChart.setExtraOffsets(0f, 0f, 0f, 10f)

        // 그룹화 설정
        val groupSpace = 0.2f // 각 그룹 사이의 공간
        val barSpace = 0.05f // 막대 사이의 공간
        val barWidth = 0.35f
        barData.barWidth = barWidth // 막대 너비 설정

        // X축의 최대값 계산
        val groupCount = steps.size
        val xAxisMaximum = groupCount + (barWidth + barSpace + groupSpace) - 0.5f

        // groupBars를 통해 그룹화
        barData.groupBars(0f, groupSpace, barSpace)
        barChart.data = barData

        // Chart의 X축 설정
        barChart.xAxis.apply {
            axisMinimum = 0f // x축 최소값
            axisMaximum = xAxisMaximum // 그룹화된 막대에 맞춰 x축 최댓값을 설정함
            isEnabled = true // x축 활성화
            valueFormatter = IndexAxisValueFormatter(listOf("월", "화", "수", "목", "금", "토", "일"))
            position = XAxis.XAxisPosition.BOTTOM
            textSize = 12f
            setDrawAxisLine(true)
            setDrawGridLines(false)
            setDrawLabels(true)
            granularity = 1f
            setCenterAxisLabels(true)
        }

        // 차트의 Y축 숨김
        barChart.axisLeft.isEnabled = false
        barChart.axisRight.isEnabled = false

        // 그래프 설명 비활성화
        barChart.description.isEnabled = false
        barChart.setFitBars(true)

        // 범례 설정
        barChart.legend.apply {
            form = Legend.LegendForm.SQUARE
            textSize = 12f
            textColor = Color.BLACK
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            orientation = Legend.LegendOrientation.HORIZONTAL
        }

        barChart.invalidate()
    }
}