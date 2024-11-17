package com.loveprofessor.recyclingapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.loveprofessor.recyclingapp.databinding.FragmentFaqBinding

class FaqFragment : Fragment() {
    private lateinit var binding: FragmentFaqBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFaqBinding.inflate(inflater, container, false)

        //코드

        return binding.root
    }
}