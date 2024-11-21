package com.loveprofessor.recyclingapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.loveprofessor.recyclingapp.databinding.FragmentCameraListBinding

class CameraListFragment : Fragment() {
    private lateinit var binding:FragmentCameraListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraListBinding.inflate(inflater, container, false)

        val collectionRef = FirebaseFirestore.getInstance().collection("classify_result")
        val query = collectionRef.whereEqualTo("userUid", MyApplication.uId)
        val listData  = ArrayList<ListData>()

        query.get().addOnSuccessListener { querySnapshot->
            // 성공적으로 데이터를 가져온 경우
            for (document in querySnapshot) {
                val result = document.toObject(ClassifyResultData::class.java)
                listData.add(ListData(result.imageURL, result.category, result.uploadDt))

                binding.recyclerViewResult.adapter = CustomAdapter(listData)
                binding.recyclerViewResult.layoutManager = LinearLayoutManager(requireContext())
            }
        }


        return binding.root
    }
}