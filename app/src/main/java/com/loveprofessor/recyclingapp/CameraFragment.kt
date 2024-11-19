package com.loveprofessor.recyclingapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.loveprofessor.recyclingapp.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {
    private lateinit var binding: FragmentCameraBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)

        // 백스택 버튼 클릭 시, nav_home(홈 화면) 으로 이동한다고만 생각하자
        // Enter시 왼쪽 -> 오른쪽으로 이동하는 애니메이션 추가
        // Popup시 오른쪽 -> 왼쪽으로 이동하는 애니메이션 추가
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_home, false)
            .setEnterAnim(R.anim.anim_slide_in_from_left_fade_in)
            .setPopEnterAnim(R.anim.anim_slide_in_from_right_fade_in)
            .build()

        // Camera Fragment에서 CameraHome Fragment로 이동
        findNavController().navigate(R.id.action_camera_to_camera_home, null, navOptions)

        return binding.root
    }
}