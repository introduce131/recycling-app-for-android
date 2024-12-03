package com.loveprofessor.recyclingapp.camera

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.loveprofessor.recyclingapp.CustomAdapter
import com.loveprofessor.recyclingapp.ListData
import com.loveprofessor.recyclingapp.MyApplication
import com.loveprofessor.recyclingapp.databinding.FragmentCameraListBinding

class CameraListFragment : Fragment() {
    private lateinit var binding:FragmentCameraListBinding
    private lateinit var listData: ArrayList<ListData>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraListBinding.inflate(inflater, container, false)

        val collectionRef = FirebaseFirestore.getInstance().collection("classify_result")
        val query = collectionRef
            .whereEqualTo("userUid", MyApplication.uId)
            //.orderBy("uploadDt", Query.Direction.DESCENDING)
        listData = ArrayList<ListData>()

        query.get().addOnSuccessListener { querySnapshot ->
            // 성공적으로 데이터를 가져온 경우
            for (document in querySnapshot) {
                val result = document.toObject(ClassifyResultData::class.java)
                listData.add(ListData(result.imageURL, result.category, result.uploadDt))

                binding.recyclerViewResult.adapter = CustomAdapter(listData)
                binding.recyclerViewResult.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        // 전체 삭제 클릭 이벤트
        // 사용한 라이브러리 : F0RIS/SweetAlertDialog(https://github.com/F0RIS/sweet-alert-dialog)
        binding.textViewDeleteAll.setOnClickListener {
            if(listData.isNotEmpty()) {
                SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                    //.setTitleText("경고")
                    .setContentText("전체 데이터를 삭제하면 모든 데이터가 영구적으로 사라집니다. 계속 하시겠습니까?")
                    .setConfirmText("확인")
                    .setCancelText("취소")
                    .setCancelClickListener { dialog ->
                        // 취소 버튼 클릭하면 dialog 창을 끔
                        dialog.dismiss()
                    }
                    .setConfirmClickListener { dialog ->
                        // 이제 취소를 처리해보자... 할게 많네
                        val db = FirebaseFirestore.getInstance()        // fireStore 인스턴스
                        val storage = FirebaseStorage.getInstance()     // Storage 인스턴스

                        // 삭제하려는 collection과 query를 생성, where 조건은 현재 로그인한 사용자의 UID
                        val collectionRef = db.collection("classify_result")
                        val query = collectionRef.whereEqualTo("userUid", MyApplication.uId)

                        // 먼저 UID를 포함하는 document를 찾는다.
                        // 왜 Log가 한글을 인식 못해서 영어만 쓰게 만드는지 모르겠다...
                        query.get().addOnSuccessListener { querySnapShot ->
                            for(document in querySnapShot) {
                                // image도 storage에서 지워야되기 때문에 URL을 따로 저장해놓음.
                                val imageURL = document.getString("imageURL")

                                // imageURL의 데이터가 있다면 지우는 처리를 진행해야 함
                                if(!TextUtils.isEmpty(imageURL)) {
                                    val storageRef = storage.getReferenceFromUrl(imageURL!!)

                                    // storage에서 imageURL에 해당하는 Image를 삭제함
                                    storageRef.delete().addOnSuccessListener {
                                        // Storage에서 이미지 삭제 성공
                                        Log.d("Storage", "Storage Image Delete Success url : ${imageURL.toString()}")

                                        // 그리고 이제 Firestore에 있는 document도 삭제해주면 됨
                                        collectionRef.document(document.id).delete()
                                            .addOnSuccessListener {
                                                // FireStore에서 문서 삭제 성공 시
                                                Log.d("FireStore", "FireStore Document Delete Success : ${document.id}")
                                            }
                                            .addOnFailureListener { exception ->
                                                // FireStore에서 문서 삭제 실패 시
                                                Log.d("FireStore", "FireStore Document Delete Failed..${exception.message}")
                                            }
                                    }.addOnFailureListener { exception ->
                                        // Storage에서 이미지 삭제 실패
                                        Log.d("Storage", "Storage Image Delete Failed..${exception.message}")
                                    }
                                } else {
                                    // 만약 문서에 ImageURL이 없으면 그냥 document만 삭제하고 끝냄
                                    collectionRef.document(document.id).delete()
                                        .addOnSuccessListener {
                                            Log.d("FireStore", "FireStore Document Delete Success : ${document.id}")
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.d("FireStore", "FireStore Document Delete Failed..${exception.message}")
                                        }
                                }
                            }
                        }
                        dialog.dismiss() // 작업 완료 후 dialog 끄기
                        findNavController().navigateUp() // 현재 프래그먼트에서 이전 화면으로 돌아가기
                    }
                    .show()
            } else {
                SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                    .setContentText("검색 결과가 없습니다!")
                    .setConfirmText("확인")
                    .show()
            }
        }

        return binding.root
    }
}