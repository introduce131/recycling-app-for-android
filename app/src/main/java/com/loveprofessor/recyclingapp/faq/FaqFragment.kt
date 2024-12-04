package com.loveprofessor.recyclingapp.faq

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.loveprofessor.recyclingapp.R
import com.loveprofessor.recyclingapp.databinding.FragmentFaqBinding

class FaqFragment : Fragment() {
    private lateinit var binding: FragmentFaqBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFaqBinding.inflate(inflater, container, false)

        // 백스택 버튼 클릭 시, nav_home(홈 화면) 으로 이동한다고만 생각하자
        // Enter시 왼쪽 -> 오른쪽으로 이동하는 애니메이션 추가
        // Popup시 오른쪽 -> 왼쪽으로 이동하는 애니메이션 추가
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_home, false)
            .setEnterAnim(R.anim.anim_slide_in_from_left_fade_in)
            .setPopEnterAnim(R.anim.anim_slide_in_from_right_fade_in)
            .build()

        // Fragment -> Fragment 이동
        findNavController().navigate(R.id.action_Faq_to_Faq_home, null, navOptions)

        return binding.root
    }
}