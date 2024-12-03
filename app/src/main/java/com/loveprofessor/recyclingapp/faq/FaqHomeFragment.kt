package com.loveprofessor.recyclingapp.faq

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.loveprofessor.recyclingapp.CustomAdapter
import com.loveprofessor.recyclingapp.FaqAdapter
import com.loveprofessor.recyclingapp.FaqListData
import com.loveprofessor.recyclingapp.ListData
import com.loveprofessor.recyclingapp.MyApplication
import com.loveprofessor.recyclingapp.camera.ClassifyResultData
import com.loveprofessor.recyclingapp.databinding.FragmentFaqHomeBinding
import okhttp3.OkHttpClient
import kotlin.math.log

data class FaqResultData (
    var seq:Long = 0,
    var question:String = "",
    var answer:String = "",
)
class FaqHomeFragment : Fragment() {

    private lateinit var binding: FragmentFaqHomeBinding
    private lateinit var listData: ArrayList<FaqListData>
    private lateinit var adapter: FaqAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFaqHomeBinding.inflate(inflater, container, false)

        val collectionRef = FirebaseFirestore.getInstance().collection("faq_list")
        val query = collectionRef
            .orderBy("seq", com.google.firebase.firestore.Query.Direction.ASCENDING)

        listData = ArrayList<FaqListData>()
        adapter = FaqAdapter(listData)

        query.get().addOnSuccessListener {querySnapshot ->
            // 성공적으로 데이터를 가져온 경우
            for (document in querySnapshot) {
                val result = document.toObject(FaqResultData::class.java)

                listData.add(FaqListData(result.question))

                binding.recyclerViewFaqResult.adapter = FaqAdapter(listData)
                binding.recyclerViewFaqResult.layoutManager = LinearLayoutManager(requireContext())
            }
        }.addOnFailureListener {exception ->
            Log.d("FaqHome", "${exception.message}")
        }

        return binding.root
    }
}