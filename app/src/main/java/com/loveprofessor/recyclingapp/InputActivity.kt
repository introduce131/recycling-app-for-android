package com.loveprofessor.recyclingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.loveprofessor.recyclingapp.databinding.ActivityInputBinding

class InputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // fragment <- 닉네임 입력받기 화면(fragment)
        var fragment = InputNicknameFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}