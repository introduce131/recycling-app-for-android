package com.loveprofessor.recyclingapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.loveprofessor.recyclingapp.databinding.FragmentFinishBinding

class FinishFragment : Fragment() {
    private lateinit var binding: FragmentFinishBinding
    private lateinit var buttonNext: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFinishBinding.inflate(inflater, container, false)
        buttonNext = binding.buttonNext

        // 회원가입에 사용했던 현재 액티비티를 종료하고 Main화면(MainActivity)로 이동함.
        buttonNext.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()  // 현재 액티비티(InputActivity)를 종료함.
        }

        return binding.root
    }
}