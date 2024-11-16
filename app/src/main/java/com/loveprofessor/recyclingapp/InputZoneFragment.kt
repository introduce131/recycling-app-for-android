package com.loveprofessor.recyclingapp

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.loveprofessor.recyclingapp.databinding.FragmentInputZoneBinding

class InputZoneFragment : Fragment() {

    private lateinit var binding: FragmentInputZoneBinding
    private lateinit var spinner: Spinner
    private lateinit var buttonNext: Button

    // 선택된 지역구를 저장할 변수
    private lateinit var selectedZone: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 뷰 바인딩 초기화
        binding = FragmentInputZoneBinding.inflate(inflater, container, false)
        spinner = binding.spinner
        buttonNext = binding.buttonNext

        // 서울 지역구 목록 배열 설정
        val zoneItems = arrayOf(
            "선택하세요", "강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구",
            "금천구", "노원구","도봉구", "동대문구", "동작구", "마포구", "서대문구", "서초구",
            "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구",
            "중구", "중랑구"
        )

        // ArrayAdapter 생성
        val myAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, zoneItems)

        // 프래그먼트의 spinner에 Adapter 설정
        spinner.adapter = myAdapter

        // spinner에서 onItemSelected(아이템 선택)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                // 선택된 지역구를 변수에 저장
                selectedZone = zoneItems[position]
                if(position == 0) {
                    buttonNext.isEnabled = false  // '선택하세요'를 선택하면 버튼 비활성화
                    buttonNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_enabled_false))
                } else {
                    buttonNext.isEnabled = true
                    buttonNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_enabled_true))
                }
            }

            // 선택되지 않았을 때
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        // 다음 프래그먼트로 이동
        buttonNext.setOnClickListener {
            val nextFragment:Fragment = InputDayFragment()  // 다음 프래그먼트를 저장

            // requireActivity() : 현재 프래그먼트가 속한 액티비티(InputActivity)를 말함
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

            /* MyApplication 클래스에 사는 지역(구)를 저장한다 */
            MyApplication.userZone = selectedZone

            // 커스텀 애니메이션 추가 (토스에서 사용하는거라는데 duration을 좀 조정하면 더 부드럽게 보일 듯)
            transaction.setCustomAnimations(
                R.anim.anim_slide_in_from_left_fade_in,
                R.anim.anim_fade_out
            )

            // FrameLayout에 요일 입력 프래그먼트(InputDayFragment)로 교체
            transaction.replace(R.id.fragment_container, nextFragment)
            transaction.addToBackStack(null)    // 뒤로가기 버튼
            transaction.commit()
        }
        // 뷰 반환
        return binding.root
    }
}
