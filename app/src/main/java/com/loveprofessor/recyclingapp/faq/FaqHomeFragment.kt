package com.loveprofessor.recyclingapp.faq

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.loveprofessor.recyclingapp.databinding.FragmentFaqHomeBinding
import okhttp3.OkHttpClient

class FaqHomeFragment : Fragment() {

    private lateinit var binding: FragmentFaqHomeBinding
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFaqHomeBinding.inflate(inflater, container, false)

        return binding.root
    }
}