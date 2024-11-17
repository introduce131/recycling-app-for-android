package com.loveprofessor.recyclingapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.loveprofessor.recyclingapp.MyApplication
import com.loveprofessor.recyclingapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.textViewIntro.text = "${MyApplication.userNickname}님 안녕하세요\n오늘은 재활용쓰레기 버리는 날이에요."
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}