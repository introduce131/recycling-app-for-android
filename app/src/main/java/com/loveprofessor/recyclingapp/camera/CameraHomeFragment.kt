package com.loveprofessor.recyclingapp.camera

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.loveprofessor.recyclingapp.databinding.FragmentCameraHomeBinding
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.loveprofessor.recyclingapp.R
import com.loveprofessor.recyclingapp.ml.ModelUnquant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder


class CameraHomeFragment : Fragment() {
    private lateinit var binding: FragmentCameraHomeBinding
    private lateinit var recycleImageView: ImageView
    private lateinit var buttonPhotoGraph: Button
    private lateinit var buttonSearchList: Button
    private lateinit var resultCategory: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentCameraHomeBinding.inflate(inflater, container, false)

        buttonPhotoGraph = binding.buttonPhotoGraph
        buttonSearchList = binding.buttonSearchList
        recycleImageView = binding.recycleImageView

        // 카메라로 사진을 촬영한다.
        // 스스로 권한을 체크해서 카메라 권한을 가지고 있다면(GRANTED) 사진을 촬영하여 ImageView에 비트맵 형식으로 사진을 띄워서 미리 보여주고
        // 만약 권한이 없으면 requestPermission 메서드를 사용해서 권한을 다시 체크하고, 권한 없으면 그냥 Toast를 띄운다.
        buttonPhotoGraph.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePicturePreview.launch(null)
            } else {
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }

        // 이전 검색 목록 리스트로 이동
        buttonSearchList.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_camera_home, false)
                .setEnterAnim(R.anim.anim_slide_in_from_left_fade_in)
                .setPopEnterAnim(R.anim.anim_slide_in_from_right_fade_in)
                .build()

            findNavController().navigate(R.id.action_camera_home_to_camera_list, null, navOptions)
        }

        return binding.root
    }

    // launch camera 랑 사진 찍기
    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {bitmap->
        if(bitmap != null) {
            recycleImageView.setImageBitmap(bitmap) // 이미지뷰에 이미지를 띄워줌
            outputGenerator(bitmap)                 // 모델을 사용할거고 입력값에 Bitmap 이미지를 넣음

            // 백스택 버튼 클릭 시, nav_camera_home(홈 화면) 으로 '다시 되돌아간다' 라고 생각하자
            // Enter시 왼쪽 -> 오른쪽으로 이동하는 애니메이션 추가
            // Popup시 오른쪽 -> 왼쪽으로 이동하는 애니메이션 추가
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_camera_home, false)
                .setEnterAnim(R.anim.anim_slide_in_from_left_fade_in)
                .setPopEnterAnim(R.anim.anim_slide_in_from_right_fade_in)
                .build()

            // 데이터를 넘길 bundle
            val bundle = Bundle().apply {
                putParcelable("imageBitmap", bitmap)    // Bitmap 데이터 전달
                putString("category", resultCategory)   // 클래스(i.e. plastic) 전달
            }

            // Camera Fragment에서 CameraHome Fragment로 이동하면서 bundle과 options도 추가
            findNavController().navigate(R.id.action_camera_home_to_camera_result, bundle, navOptions)
        }
    }

    // 카메라 권한 요청, 권한 있으면 사진 촬영으로 넘어가고, 아니면 그냥 Toast 띄우고 끝냄. 수정을 할까 말까
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {granted->
        if (granted) {
            takePicturePreview.launch(null)
        } else {
            Toast.makeText(requireContext(), "권한이 거부됨!", Toast.LENGTH_SHORT).show()
        }
    }

    // 비트맵 이미지를 리사이즈해야 함 224x224, 그리고 리사이즈한 이미지를 반환함
    // 1. 224x224 사이즈로 모델을 학습시켜서 input되는 이미지도 224x224 크기로 resize를 해야함. 그래야 정확한 결과를 얻을 수 있음.
    // 2. 모델을 .png로 학습시켜서 비율 및 품질도 유지하면서 리사이즈 해야한다. 깨지면 이미지를 인식하기 어렵고 부정확한 결과가 계속 나옴
    private fun resizeBitmapWithPadding(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // 원본 이미지의 가로 세로 비율을 계산하여 aspectRatio에 저장
        val aspectRatio = width.toFloat() / height.toFloat()

        // targetWidth(224), targetHeight(224)는 목표 리사이즈 크기고 이걸 각각 변수에 저장함
        var newWidth = targetWidth
        var newHeight = targetHeight

        // 가로 세로 비율을 유지하며 크기 조정
        if (aspectRatio > 1) {
            newHeight = (targetWidth / aspectRatio).toInt()
        } else {
            newWidth = (targetHeight * aspectRatio).toInt()
        }

        // 목표크기(224, 224)만큼의 새로운 이미지를 생성 resizedBitmap에 저장함
        // filter 값이 true라면 부드럽게 리사이즈하여 자연스럽게 리사이즈가 된다.
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        // 비율을 맞추고, 배경을 채울 빈 영역을 계산하고, 빈 배경 이미지를 생성했음
        val resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val left = (targetWidth - newWidth) / 2     // 가로 중심 맞추기
        val top = (targetHeight - newHeight) / 2    // 세로 중심 맞추기

        // 여백 배경 색상을 (검정색이나 흰색)으로 채우면 되는데 학습시킨 이미지들이 흰색배경이 많으니 흰색으로 설정했음
        canvas.drawColor(Color.WHITE)

        // 리사이즈된 이미지를 배경 위에 그린다, 그리고 여백이 흰색으로 채워진 리사이즈된 비트맵 이미지를 return 함
        canvas.drawBitmap(resizedBitmap, left.toFloat(), top.toFloat(), null)

        return resultBitmap
    }

    // Bitmap 이미지를 입력받아서, 그에 따른 분류를 처리함
    private fun outputGenerator(bitmap: Bitmap) {
        // TFLite 모델의 인스턴스를 생성, model
        val model = ModelUnquant.newInstance(requireContext())

        // Bitmap을 비율을 유지하며 224x224(목표 크기)로 리사이즈하고 배경을 흰색으로 채우기
        val resizedBitmap = resizeBitmapWithPadding(bitmap, 224, 224)

        // Bitmap을 TensorImage로 변환하여 tfImage에 저장함
        // TFImage : TensorFlow Lite에서 사용할 수 있는 이미지 형태라고 보면 됨
        val tfImage = TensorImage.fromBitmap(resizedBitmap)

        // 모델의 입력 텐서인 TensorBuffer 생성 (모델의 입력 크기(224,224)에 맞게 설정)
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

        // Bitmap에서 ByteBuffer로 변환 후 TensorBuffer에 로드를 시켜줌
        // ByteBuffer를 사용해서 모델에 데이터를 전달해야 한다.
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3) // 4는 float32의 크기
        byteBuffer.order(ByteOrder.nativeOrder())   // 이건 오더로 메모리 순서를 정한다고 하는데 잘 모르겠네...
        val intValues = IntArray(224 * 224)

        // Bitmap에서 각 픽셀의 RGB 값을 읽어들여서(getPixcels) ByteBuffer에 저장함
        // 이미지의 각 픽셀 색상을 intValues 배열에 저장하고, RED, GREEN, BLUE 순서로 0~1사이로 정규화해서 ByteBuffer에 저장을 한다.
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
        intValues.forEachIndexed { index, color ->
            byteBuffer.putFloat(((color shr 16) and 0xFF) / 255.0f)  // Red
            byteBuffer.putFloat(((color shr 8) and 0xFF) / 255.0f)   // Green
            byteBuffer.putFloat((color and 0xFF) / 255.0f)           // Blue
        }

        // 이제 입력 텐서에 ByteBuffer를 로드한다. 이걸로 모델에 텐서를 전달할 수 있음
        inputFeature0.loadBuffer(byteBuffer)

        // 모델에 입력데이터 넣고, 예측을 실행한다고 하는데. 그냥 결과 받아온다고 생각하면 편함
        val outputs = model.process(inputFeature0)

        // 결과에서 출력 텐서를 추출 (입력값도 출력값도 텐서버퍼임, 여기에 확률 값을 갖고있음)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        // 출력 텐서에서 확률 값을 가져와 가장 높은 확률의 카테고리 추출
        val probability = outputFeature0.floatArray

        // maxByOrNull을 사용해서 가장 높은 확률을 가진 인덱스를 여기에 저장함
        val maxIndex = probability.indices.maxByOrNull { probability[it] } ?: -1

        // 그리고 확률 값을 maxScore에 저장함, 예시를 들면 Plastic : 99.999123%
        val maxScore = probability[maxIndex]

        // 로그로 출력 값 확인
        Log.i("Prediction", "Probabilities: ${probability.joinToString(", ")}")
        Log.i("Prediction", "Max Index: $maxIndex, Max Score: $maxScore")

        // 클래스 레이블 맵핑 예시
        val labels = listOf(
            "aluminum-can", "aluminum-foil", "glass-bottle", "glass-jar", "paper-carbon", "paper-cardboard", "paper-common",
            "plastic-bags", "plastic-bottle", "plastic-container", "plastic-disposable", "plastic-resin-bottle"
        )

        // 예측된 클래스 이름
        val predictedLabel = labels.getOrElse(maxIndex) { "Unknown" }

        // 결과 출력 (예시로 가장 높은 확률의 카테고리 출력)
        val resultText = StringBuilder()
        resultText.append("클래스 이름: $predictedLabel\n")
        resultText.append("확률 추론: ${maxScore * 100}%")

        // 다음 프래그먼트로 넘길 결과 예)plastic
        resultCategory = predictedLabel

        //tvOutput.text = resultText.toString()

        // 모델 리소스 해제
        model.close()
    }
}