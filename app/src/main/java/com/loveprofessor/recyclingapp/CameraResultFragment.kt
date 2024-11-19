package com.loveprofessor.recyclingapp

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.ParcelCompat
import com.loveprofessor.recyclingapp.databinding.FragmentCameraHomeBinding
import com.loveprofessor.recyclingapp.databinding.FragmentCameraResultBinding

class CameraResultFragment : Fragment() {
    private lateinit var binding: FragmentCameraResultBinding
    private var bitmapImage: Bitmap? = null
    private var category: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentCameraResultBinding.inflate(inflater, container, false)

        // 전환되기 전 fragment인 CameraHomeFragment에서 넘어온 인자(arguments)를 bundle에 저장함
        val bundle = arguments

        // 이건 진짜 이렇게까지 해야되나 싶긴한데 getParcelable 함수가 API33(티라미수) 이하 버전이랑 이상버전일 때 넘기는 인자값이 다름
        // 그래서 33 이상과 이하일 때 각각 처리를 따로 해줘야 한다
        // 진짜 조건을 저렇게 설정해주니까 빨간줄이 사라지네.... 신기하다
        // 아무튼 화면에 보여줄 bitmap 이미지
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bitmapImage = bundle?.getParcelable<Bitmap>("imageBitmap", Bitmap::class.java)
        } else {
            // API 33 미만에서는 그냥 원래대로 처리하면 됨, 물론 코드는 취소선으로 표시되겠지만...
            bitmapImage = bundle?.getParcelable<Bitmap>("imageBitmap")
        }

        // 모델의 출력결과로 나온 '분류', plastic, glass-jar 등이 여기에 포함 된다.
        category = bundle?.getString("category")

        // 카테고리를 표시함
        binding.textViewCategory.text = category

        // 이미지를 표시함
        binding.recycleImageView.setImageBitmap(bitmapImage)

        return binding.root
    }
}