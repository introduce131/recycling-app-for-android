package com.loveprofessor.recyclingapp.faq

import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.loveprofessor.recyclingapp.FaqListData
import com.loveprofessor.recyclingapp.MyApplication
import com.loveprofessor.recyclingapp.R
import com.loveprofessor.recyclingapp.databinding.FragmentFaqAnswerBinding

class FaqAnswerFragment : Fragment() {
    private lateinit var binding: FragmentFaqAnswerBinding
    private var seq:Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentFaqAnswerBinding.inflate(inflater, container, false)


        val bundle = arguments
        seq = bundle?.getInt("position")?.plus(1)

        val collectionRef = FirebaseFirestore.getInstance().collection("faq_list")
        val query = collectionRef.whereEqualTo("seq", seq)

        query.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                val result = document.toObject(FaqResultData::class.java)

                if(result != null) {
                    binding.faqQuestion.text = result.question
                    binding.faqAnswer.text = result.answer.replace("\\n", "\n")
                    break
                }
            }
        }

        return binding.root
    }
}