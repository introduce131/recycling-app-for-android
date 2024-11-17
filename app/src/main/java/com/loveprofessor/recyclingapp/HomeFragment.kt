package com.loveprofessor.recyclingapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.loveprofessor.recyclingapp.MyApplication
import com.loveprofessor.recyclingapp.databinding.FragmentHomeBinding
import java.time.LocalDate

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var textViewIntro: TextView
    private lateinit var menuCamera: CardView
    private lateinit var menuReport: CardView
    private lateinit var menuFAQ: CardView
    private lateinit var menuPointSearch: CardView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        menuCamera = binding.itemCardCamera
        menuReport = binding.itemCardReport
        menuFAQ = binding.itemCardFaq
        menuPointSearch = binding.itemCardPointSearch
        textViewIntro = binding.textViewIntro

        lateinit var introText:String

        /* 오늘 날짜랑 쓰레기 버리는 날짜랑 비교해서 상단 텍스트에 표시할거임. */
        if(MyApplication.userGarbageday.isNotEmpty()) {
            //var days:List<String> = MyApplication.userGarbageday.split(",")
            var days:MutableList<String> = MyApplication.userGarbageday.split(",").toMutableList()
            days.add("일요일")
            var today = LocalDate.now().dayOfWeek.toString()    // 오늘 날짜에 해당하는 요일을 today에 저장함 (영어)

            // 영어로 된 요일을 한글로 매핑 시켜주는 작업을 해야 함
            val daysOfKorean = mapOf(
                "MONDAY" to "월요일",  "TUESDAY" to "화요일",  "WEDNESDAY" to "수요일",
                "THURSDAY" to "목요일",  "FRIDAY" to "금요일",  "SATURDAY" to "토요일",  "SUNDAY" to "일요일"
            )
            today = daysOfKorean[today].toString()

            // 이제 오늘 날짜랑 쓰레기버리는 날짜랑 비교하면 됨
            for(day in days) {
                if(day == today) {
                    introText = "${MyApplication.userNickname}님 안녕하세요!\n${today}은 재활용쓰레기\n버리는 날이에요."
                } else {
                    introText = "${MyApplication.userNickname}님 안녕하세요!\n오늘도 좋은 하루 되세요!"
                }
            }
        }

        textViewIntro.text = introText  // 그리고 이제 ViewIntro에 텍스트를 표시

        // '검색' 메뉴 클릭 시 이벤트 (카메라 촬영)
        menuCamera.setOnClickListener {

        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}