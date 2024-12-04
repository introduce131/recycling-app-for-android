package com.loveprofessor.recyclingapp.faq

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.loveprofessor.recyclingapp.FaqAdapter
import com.loveprofessor.recyclingapp.FaqListData
import com.loveprofessor.recyclingapp.R
import com.loveprofessor.recyclingapp.databinding.FragmentFaqHomeBinding

data class FaqResultData(
    var seq: Long = 0,
    var question: String = "",
    var answer: String = ""
)

class FaqHomeFragment : Fragment(), FaqAdapter.OnItemClickListener {

    private lateinit var binding: FragmentFaqHomeBinding
    private lateinit var listData: ArrayList<FaqListData>
    private lateinit var adapter: FaqAdapter
    private lateinit var searchList: ArrayList<FaqListData>
    private lateinit var originalList: ArrayList<FaqListData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFaqHomeBinding.inflate(inflater, container, false)

        val collectionRef = FirebaseFirestore.getInstance().collection("faq_list")
        val query = collectionRef.orderBy("seq", com.google.firebase.firestore.Query.Direction.ASCENDING)

        listData = ArrayList()
        searchList = ArrayList()
        originalList = ArrayList()
        adapter = FaqAdapter(listData, this)

        query.get().addOnSuccessListener { querySnapshot ->
            // 성공적으로 데이터를 가져온 경우
            for (document in querySnapshot) {
                val result = document.toObject(FaqResultData::class.java)
                val faqData = FaqListData(result.question)
                originalList.add(faqData)
                listData.add(faqData)
            }
            adapter.setItems(originalList)
            binding.recyclerViewFaqResult.adapter = adapter
            binding.recyclerViewFaqResult.layoutManager = LinearLayoutManager(requireContext())
            Log.d("FaqHome", "Original List Size: ${originalList.size}")
        }.addOnFailureListener { exception ->
            Log.d("FaqHome", "${exception.message}")
        }

        // EditText의 텍스트 변경 감지
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                Log.d("FaqHome", "Search Text: $searchText")
                searchList.clear()

                if (searchText.isEmpty()) {
                    adapter.setItems(originalList)
                    Log.d("FaqHome", "Adapter set with original list, size: ${originalList.size}")
                } else {
                    for (item in originalList) {
                        if (item.list_question.trim().contains(searchText)) {
                            searchList.add(item)
                        }
                    }
                    adapter.setItems(searchList)
                    Log.d("FaqHome", "Adapter set with search list, size: ${searchList.size}")
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    override fun onItemClick(view: View, position: Int) {
        // 백스택 버튼 클릭 시, nav_home(홈 화면) 으로 이동한다고만 생각하자
        // Enter시 왼쪽 -> 오른쪽으로 이동하는 애니메이션 추가
        // Popup시 오른쪽 -> 왼쪽으로 이동하는 애니메이션 추가
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_Faq_home, false)
            .setEnterAnim(R.anim.anim_slide_in_from_left_fade_in)
            .setPopEnterAnim(R.anim.anim_slide_in_from_right_fade_in)
            .build()

        val bundle = Bundle().apply {
            putInt("position", position)
        }

        //Fragment -> Fragment 이동
        findNavController().navigate(R.id.action_Faq_home_to_Faq_answer, bundle, navOptions)
    }
}
