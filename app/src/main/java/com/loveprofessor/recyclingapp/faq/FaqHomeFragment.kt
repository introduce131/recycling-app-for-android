package com.loveprofessor.recyclingapp.faq

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.loveprofessor.recyclingapp.CustomAdapter
import com.loveprofessor.recyclingapp.FaqAdapter
import com.loveprofessor.recyclingapp.FaqListData
import com.loveprofessor.recyclingapp.ListData
import com.loveprofessor.recyclingapp.MyApplication
import com.loveprofessor.recyclingapp.R
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
    private lateinit var searchList: ArrayList<FaqListData>
    private lateinit var originalList: ArrayList<FaqListData>
    private lateinit var editTextSearch: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFaqHomeBinding.inflate(inflater, container, false)

        editTextSearch = binding.editTextSearch
        editTextSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0)
        editTextSearch.compoundDrawablePadding = 16

        val collectionRef = FirebaseFirestore.getInstance().collection("faq_list")
        val query = collectionRef.orderBy("seq", com.google.firebase.firestore.Query.Direction.ASCENDING)

        listData = ArrayList()
        searchList = ArrayList()
        originalList = ArrayList()
        adapter = FaqAdapter(listData)
        binding.recyclerViewFaqResult.adapter = adapter
        binding.recyclerViewFaqResult.layoutManager = LinearLayoutManager(requireContext())

        query.get().addOnSuccessListener {querySnapshot ->
            // 성공적으로 데이터를 가져온 경우
            for (document in querySnapshot) {
                val result = document.toObject(FaqResultData::class.java)
                listData.add(FaqListData(result.question))
                originalList.add(FaqListData(result.question))
            }
            adapter.setItems(originalList)
        }.addOnFailureListener {exception ->
            Log.d("FaqHome", "${exception.message}")
        }

        binding.editTextSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = p0.toString().trim()
                Log.d("FaqHome", "Search Text: $searchText")
                searchList.clear()

                if(searchText.isEmpty()) {
                    adapter.setItems(originalList)
                } else {
                    for(item in originalList) {
                        if(item.list_question.trim().contains(searchText)) {
                            searchList.add(item)
                        }
                    }
                    adapter.setItems(searchList)
                    Log.d("FaqHome", "searchList : $searchList")
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        return binding.root
    }
}