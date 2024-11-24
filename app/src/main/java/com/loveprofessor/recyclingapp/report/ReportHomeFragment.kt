package com.loveprofessor.recyclingapp.report

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.loveprofessor.recyclingapp.CustomMarkerView
import com.loveprofessor.recyclingapp.R
import com.loveprofessor.recyclingapp.databinding.FragmentReportHomeBinding
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

class ReportHomeFragment : Fragment() {

    private lateinit var binding: FragmentReportHomeBinding
    private lateinit var stepCountTextView: TextView
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReportHomeBinding.inflate(inflater, container, false)

        stepCountTextView = binding.stepCountTextView
        barChart = binding.barChart
        val markerView = CustomMarkerView(requireContext(), R.layout.custom_marker)
        barChart.marker = markerView

        // 드래그, 확대/축소 비활성화 (너무 불편함)
        barChart.setDragEnabled(false)
        barChart.setScaleEnabled(false)

        // 걸음수 데이터 생성
        val steps = listOf(
            BarEntry(0f, 1000f),
            BarEntry(1f, 2000f),
            BarEntry(2f, 3000f),
            BarEntry(3f, 4500f),
            BarEntry(4f, 5500f),
            BarEntry(5f, 6000f),
            BarEntry(6f, 7000f)
        )

        // 탄소 절감량 데이터 생성
        val carbonreduction = listOf(
            BarEntry(0f, 500f),
            BarEntry(1f, 600f),
            BarEntry(2f, 700f),
            BarEntry(3f, 800f),
            BarEntry(4f, 955f),
            BarEntry(5f, 1000f),
            BarEntry(6f, 1100f)
        )

        setChartBar(steps, carbonreduction)

        // SharedPreferences에서 걸음 수 가져오기 (초기값 설정)
        val prefs: SharedPreferences = requireContext().getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        val stepCount = prefs.getInt("step_count", 0)

        // 초기 걸음 수 표시
        stepCountTextView.text = "$stepCount"

        return binding.root
    }

    // MainActivity로부터 걸음 수를 업데이트 받는 메서드
    fun updateStepCount(stepCount: Int) {
        // 걸음 수를 SharedPreferences에 저장
        val prefs: SharedPreferences = requireContext().getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("step_count", stepCount).apply()

        // UI 업데이트
        stepCountTextView.text = "걸음 수 : $stepCount"
    }

    // 내가 속한 주(week)의 날짜를 계산해서 return 해주는 함수를 만들음
    // 그리고 날짜 배열을 return 함. (2024-11-18 ~ 2024-11-24)
    private fun getCurrentWeekDays(): MutableList<String> {
        val today = LocalDate.now() // 오늘 날짜 저장할 today
        val weekDays:MutableList<String> = mutableListOf()  // 이번주 날짜들을 저장할 List

        // 오늘(today)를 기준으로 이번주의 시작요일인 월요일을 먼저 구해야 함
        // today.dayOfWeek.value 는 만약 오늘이 일요일이면 숫자 7을 반환하고 DayofWeek.MONDAY.value는 월요일인 1을 반환함.
        // 그러면 (7 - 1) = 6이고, 오늘(일요일)에서 minusDays로 6을 빼면 월요일 이라는 값이 나옴.
        // 그러면 이번주의 월요일을 (YYYY-MM-DD) 형식으로 반환하고 그 값이 monday에 저장됨.
        val monday = today.minusDays((today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

        // formatter를 하나 만들어줘야됨. (YYYY-MM-DD) 형식으로
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // 월요일부터 i(0~6)일만큼 더한 YYYY-MM-DD로 형식을 변환하고 List에 하나씩 저장함
        for(i in 0..6) {
            val date = monday.plusDays(i.toLong()).format(formatter)
            weekDays.add(date)
        }

        return weekDays
    }

    // 걸음수와 탄소저감량을 인자로 받고, Chart를 세팅해주는 함수 setChartBar
    private fun setChartBar(steps: List<BarEntry>, carbons: List<BarEntry>) {
        val stepsDataSet = BarDataSet(steps, "걸음수").apply {
            color = Color.parseColor("#9CD690")
            valueTextColor = Color.BLACK
            setDrawValues(false) // 값 표시 비활성화
        }

        val carbonReductionDataSet = BarDataSet(carbons, "탄소 절감량").apply {
            color = Color.parseColor("#9898EC")
            valueTextColor = Color.BLACK
            setDrawValues(false) // 값 표시 비활성화
        }

        // 2개의 DataSet 객체를 결합하여 차트에 표시를 하고 갱신한다(invalidate())
        val barData = BarData(stepsDataSet, carbonReductionDataSet).apply {
            barWidth = 0.3f
        }

        barChart.data = barData
        barChart.invalidate()

        // 차트의 그룹화 설정
        val groupSpace = 0.2f // 각 그룹 사이의 공간
        val barSpace = 0.05f // 막대 사이의 공간
        val barWidth = 0.35f
        barData.barWidth = barWidth // 막대 너비 설정

        // X축의 최대값 계산
        val groupCount = steps.size
        val xAxisMaximum = groupCount + (barWidth + barSpace + groupSpace) - 0.5f  // 일단 오른쪽 여백떄문에 임시로 -0.5f 했는데 이게 해결방법은 아님
        //val xAxisMaximum = steps.size.toFloat() + groupSpace // x축의 최대값 조정

        // groupBars를 통해 그룹화
        barData.groupBars(0f, groupSpace, barSpace)  // x값 0부터 시작, groupSpace와 barSpace는 차트 사이의 간격
        barChart.data = barData

        // 차트 하단에 추가 여백
        barChart.apply {
            barChart.setExtraOffsets(0f, 0f, 0f, 20f) // 상하좌우 여백 조정
        }

        // Chart의 X축 설정
        barChart.xAxis.apply {
            axisMinimum = 0f // x축 최소값
            axisMaximum = xAxisMaximum // 그룹화된 막대에 맞춰 x축 최댓값을 설정함
            isEnabled = true // x축 활성화

            val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")
            valueFormatter = IndexAxisValueFormatter(daysOfWeek) // 요일 표시

            position = XAxis.XAxisPosition.BOTTOM // x축 위치
            textSize = 12f
            setDrawAxisLine(true) // x축의 축선
            setDrawGridLines(false) // x축의 그리드
            setDrawLabels(true) // x축의 레이블
            granularity = 1f // x축에서 각 레이블 사이의 최소 간격
            setCenterAxisLabels(true) // x축 레이블 가운데 정렬
        }

        barData.groupBars(0f, groupSpace, barSpace)
        barChart.data = barData

        barChart.invalidate()

        // 차트의 Y축 숨김
        barChart.axisLeft.isEnabled = false
        barChart.axisRight.isEnabled = false

        // 그래프 설명 비활성화
        barChart.description.isEnabled = false
        barChart.setFitBars(true) // 차트 데이터 자동 조정

        // 범례 설정
        val legend = barChart.legend
        legend.form = Legend.LegendForm.SQUARE
        legend.textSize = 12f
        legend.textColor = Color.BLACK
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT // 오른쪽 정렬
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP // 상단 정렬
        legend.orientation = Legend.LegendOrientation.HORIZONTAL // 배치 방향
    }

}
