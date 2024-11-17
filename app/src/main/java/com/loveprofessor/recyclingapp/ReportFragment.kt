package com.loveprofessor.recyclingapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.loveprofessor.recyclingapp.databinding.FragmentCameraBinding
import com.loveprofessor.recyclingapp.databinding.FragmentReportBinding

class ReportFragment : Fragment() {
    private lateinit var binding: FragmentReportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentReportBinding.inflate(inflater, container, false)

        // 코드 작성

        return binding.root
    }
}