package com.loveprofessor.recyclingapp

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            // 데이터셋에 따라 다른 텍스트를 표시
            val dataLabel = when (highlight?.dataSetIndex) {
                0 -> "${e.y.toInt()} 걸음" // 첫 번째 데이터셋(걸음수)
                1 -> "${e.y.toInt()}g" // 두 번째 데이터셋(탄소 절감량)
                else -> "데이터 없음"
            }
            tvContent.text = dataLabel
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}

