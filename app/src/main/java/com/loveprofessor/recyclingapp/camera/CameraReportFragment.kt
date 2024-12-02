package com.loveprofessor.recyclingapp.camera

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.loveprofessor.recyclingapp.HomeFragment
import com.loveprofessor.recyclingapp.R
import com.loveprofessor.recyclingapp.databinding.FragmentCameraReportBinding
import java.io.ByteArrayOutputStream
import java.util.UUID

class CameraReportFragment : Fragment() {
    private lateinit var binding: FragmentCameraReportBinding
    private lateinit var selectedCategory:String
    private var bitmapImage: Bitmap? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraReportBinding.inflate(inflater, container, false)

        // 전환되기 전 fragment인 CameraHomeFragment에서 넘어온 인자(arguments)를 bundle에 저장함
        val bundle = arguments

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bitmapImage = bundle?.getParcelable<Bitmap>("imageBitmap", Bitmap::class.java)
        } else {
            // API 33 미만에서는 그냥 원래대로 처리하면 됨, 물론 에디터에서는 코드가 취소선으로 표시되겠지만...
            bitmapImage = bundle?.getParcelable<Bitmap>("imageBitmap")
        }

        // 이전 프래그먼트에서 받아온 bitmap으로 사진을 띄움
        binding.reportImageView.setImageBitmap(bitmapImage)

        val categoryItems = arrayOf(
            "선택하세요", "캔류", "쿠킹호일류", "병류(유리)", "용기류(유리)", "병류(플라스틱)", "용기류(플라스틱)",
            "비닐류", "종이팩류", "종이상자류", "종이류", "일회용 식기류")

        val myAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryItems)
        binding.spinner.adapter = myAdapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedCategory = categoryItems[position]
                if(position == 0) {
                    binding.buttonSubmit.isEnabled = false  // '선택하세요'를 선택하면 버튼 비활성화
                    binding.buttonSubmit.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_enabled_false))
                } else {
                    binding.buttonSubmit.isEnabled = true
                    binding.buttonSubmit.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_enabled_true))
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        binding.buttonSubmit.setOnClickListener {
            // storage에 파일 업로드할 객체를 여기서 생성함
            val storage: FirebaseStorage = Firebase.storage
            val storageRef: StorageReference = storage.reference

            // 업로드할 이미지의 파일 경로를 생성함 Storage/upload_images_무작위id_image.png 로 저장될거임.
            val filePath = "report_images/${selectedCategory}/${UUID.randomUUID()}_image.png"

            // 위에서 생성한 경로에 해당하는 참조 객체(Ref)를 만듦. 이 imgRef로 Storage에 업로드하면 됨
            val imgRef: StorageReference = storageRef.child(filePath)

            // 업로드된 이미지의 경로? 쨌든 url을 받아올 변수 imgURL를 미리 선언해놓음, null도 허용
            var imgURL:String? = null

            // 기존의 BitmapImage를 PNG 포맷으로 압축하고, ByteArrayOutputStream()을 사용해서 바이트 배열로 변환함
            // 이 바이트 배열 데이터(data)를 Storage에 업로드 한다.
            val baos = ByteArrayOutputStream()
            bitmapImage?.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()

            // putBytes를 사용해서 Storage에 업로드를 하고 이제 성공, 실패에 따른 이벤트 처리를 해주자
            var uploadTask = imgRef.putBytes(data)

            // 업로드 자체가 실패 시,
            uploadTask.addOnFailureListener {
                Log.d("Storage", "upload failed..")
            }.addOnCompleteListener { taskSnapShot->
                // 여긴 업로드는 성공했을 시,
                if(taskSnapShot.isSuccessful) {
                    // 업로드에 성공 후, Download URL을 가져옴
                    imgRef.downloadUrl.addOnSuccessListener { uri ->
                        if(isAdded) {
                            imgURL = uri.toString() /* <-- 여기서 imgURL을 저장하는 것임. */
                            Log.d("Storage", "URL:$imgURL")
                        }
                        /** 백스택의 저장된 내용을 전부 지우고 이동하는 방법
                         * 참고 : https://stackoverflow.com/questions/75945833/how-to-pop-up-to-start-destination-compose-navigation **/
                        SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                            .setContentText("오류결과 전송이 완료되었습니다.")
                            .setConfirmText("확인")
                            .setConfirmClickListener { dialog ->
                                val navController = findNavController()
                                navController.navigate(R.id.nav_home, null, navOptions {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true } // 이전 프래그먼트 제거
                                })
                                dialog.dismiss()
                            }
                            .show()
                    }.addOnFailureListener{ exception ->
                        // url 다운로드 실패
                        Log.d("Storage", "Download URL Failed : ${exception.message}")
                    }
                } else { // 업로드 실패
                    Log.d("Storage", "upload failed..")
                    SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                        .setContentText("오류결과 전송에 실패했습니다. (업로드 실패)")
                        .setConfirmText("확인")
                        .setConfirmClickListener { dialog ->
                            val navController = findNavController()
                            navController.navigate(R.id.nav_home, null, navOptions {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true } // 이전 프래그먼트 제거
                            })
                            dialog.dismiss()
                        }
                        .show()
                }
            }   // end of Upload
        }

        return binding.root
    }
}