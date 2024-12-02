package com.loveprofessor.recyclingapp.camera

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.loveprofessor.recyclingapp.MyApplication
import com.loveprofessor.recyclingapp.R
import com.loveprofessor.recyclingapp.databinding.FragmentCameraResultBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID


// data Class ClassifyResultData
data class ClassifyResultData (
    var userUid:String = "",
    var imageURL:String = "",
    var category:String = "",
    var uploadDt:String = ""
)

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

        /** fragment에 메뉴 추가하는 코드
         * https://velog.io/@abc9985/AndroidsetHasOptionsMenu-deprecated-%EB%90%98%EC%97%88%EC%9C%BC%EB%8B%88-addMenuProvider-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0-fragment
         * 궁금하면 한 번 읽어보는게 이해에 도움이 될 듯하다. **/
        val menuHost:MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.report_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    // 검색결과 오류신고, 새 프래그먼트로 이동할거임.
                    R.id.action_image_report -> {
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.nav_camera_result, false)
                            .setEnterAnim(R.anim.anim_slide_in_from_left_fade_in)
                            .setPopEnterAnim(R.anim.anim_slide_in_from_right_fade_in)
                            .build()

                        // 이미지 받아와서 업로드해야되니까 bundle로 bitmapImage값을 넘김
                        val bundle = Bundle().apply {
                            putParcelable("imageBitmap", bitmapImage)    // Bitmap 데이터 전달
                        }

                        findNavController().navigate(R.id.action_camera_result_to_camera_report, bundle, navOptions)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED) // <- Fragment의 뷰가 활성화된 후, 상호작용 가능한 상태에서만 메뉴를 표시한다는 의미다.

        // 이건 진짜 이렇게까지 해야되나 싶긴한데 getParcelable 함수가 API33(티라미수) 이하 버전이랑 이상버전일 때 넘기는 인자값이 다름
        // 그래서 33 이상과 이하일 때 각각 처리를 따로 해줘야 한다
        // 진짜 조건을 저렇게 설정해주니까 빨간줄이 사라지네.... 신기하다
        // 아무튼 화면에 보여줄 bitmap 이미지
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bitmapImage = bundle?.getParcelable<Bitmap>("imageBitmap", Bitmap::class.java)
        } else {
            // API 33 미만에서는 그냥 원래대로 처리하면 됨, 물론 에디터에서는 코드가 취소선으로 표시되겠지만...
            bitmapImage = bundle?.getParcelable<Bitmap>("imageBitmap")
        }

        // 모델의 출력결과로 나온 '분류', plastic, glass-jar 등이 여기에 포함 된다.
        category = bundle?.getString("category")

        // 폴리에틸렌의 결과랑 일반 페트병의 결과랑 합쳐놔서 plastic-bottle이랑 plastic-resin-bottle이랑 동일한 결과를 보여줄거임.
        if(category == "plastic-resin-bottle") category = "plastic-bottle"

        // fireStore에서 재활용품 배출방법을 가져옴, document 이름과 category 이름은 동일하다.
        val db = FirebaseFirestore.getInstance()
        db.collection("recycle_howto").document(category.toString()).get()
            .addOnSuccessListener { document ->
                if(document != null) {  // if, 문서가 존재 한다면
                    val rCategory = document.getString("recy_category")         // 카테고리 (종이류, 페트병류 등)
                    val howto = document.getString("recy_howto")                // 분리배출 방법
                    val ableItems = document.getString("recy_able_items")       // 재활용 가능 품목
                    val unableItems = document.getString("recy_unable_items")   // 재활용 불가능 품목
                    val fakeItems = document.getString("recy_fake_items")       // 재활용인척 하는 쓰레기
                    val remark = document.getString("recy_remark")              // 비고(remark)

                    // 로그로 찍기 (디버깅용)
                    Log.d("FireStore", "rCategory: $rCategory")
                    Log.d("FireStore", "howto: $howto")
                    Log.d("FireStore", "ableItems: $ableItems")
                    Log.d("FireStore", "unableItems: $unableItems")
                    Log.d("FireStore", "fakeItems: $fakeItems")
                    Log.d("FireStore", "remark: $remark")

                    binding.textViewCategory.text = rCategory ?: "No Category"        // 분류 결과를 표시한다.
                    binding.textViewHowToRecycle.text = howto?.replace("\\n", "\n")

                    // 코드 개판이네 함수로 묶어야되는데
                    /* 1. 재활용 가능 품목에 데이터가 있다면 -> 해당 부분을 표시한다. 아니면 다시 숨기기 */
                    if(!TextUtils.isEmpty(ableItems)) {
                        binding.textViewRecycleAble.visibility = View.VISIBLE
                        binding.textViewRecycleAbleLabel.visibility = View.VISIBLE
                        binding.textViewRecycleAble.text = ableItems
                    } else {
                        binding.textViewRecycleAble.visibility = View.GONE
                        binding.textViewRecycleAbleLabel.visibility = View.GONE
                    }

                    /* 2. 재활용 불가능 품목 Visible/Gone 처리 */
                    if(!TextUtils.isEmpty(unableItems)) {
                        binding.textViewRecycleUnable.visibility = View.VISIBLE
                        binding.textViewRecycleUnableLabel.visibility = View.VISIBLE
                        binding.textViewRecycleUnable.text = unableItems
                    } else {
                        binding.textViewRecycleUnable.visibility = View.GONE
                        binding.textViewRecycleUnableLabel.visibility = View.GONE
                    }

                    /* 3. 재활용인척 하는 쓰레기 품목 Visible/Gone 처리 */
                    if(!TextUtils.isEmpty(fakeItems)) {
                        binding.textViewRecycleFake.visibility = View.VISIBLE
                        binding.textViewRecycleFakeLabel.visibility = View.VISIBLE
                        binding.textViewRecycleFake.text = fakeItems
                    } else {
                        binding.textViewRecycleFake.visibility = View.GONE
                        binding.textViewRecycleFakeLabel.visibility = View.GONE
                    }

                    /* 4. 비고 Visible/Gone 처리 */
                    if(!TextUtils.isEmpty(remark)) {
                        binding.textViewRecycleRemark.visibility = View.VISIBLE
                        binding.textViewRecycleRemarkLabel.visibility = View.VISIBLE
                        binding.textViewRecycleRemark.text = remark
                    } else {
                        binding.textViewRecycleRemark.visibility = View.GONE
                        binding.textViewRecycleRemarkLabel.visibility = View.GONE
                    }
                } else {    // document가 없으면
                    Log.e("ResultFragment", "Document Not Found")
                }
            }
            .addOnFailureListener {exception ->
                Log.d("FireStore", exception.localizedMessage)
            }

        // 이미지를 표시함
        binding.recycleImageView.setImageBitmap(bitmapImage)

        // imgURL을 받고 FireStore에 저장하는 함수임.
        // imgURL을 저장하는 부분이 비동기적으로 처리되기 때문에 함수로 묶어서 addOnSuccessListener 안에 던져 놔야 해결될 듯
        fun insertFireStore(imgURL: String?) {
            // 먼저 업로드 날짜(를 여기에서 구해야 함
            val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            val date = sdf.format(System.currentTimeMillis())

            // FireStore에 데이터를 저장할 준비를 함, data class ClassifyResultData는 위에 선언되어 있음
            var classifyResultData = ClassifyResultData (
                userUid = MyApplication.uId,    // 로그인된 사용자의 고유 ID
                imageURL = imgURL ?: "",        // 아까 업로드 성공하고 받아온 url
                category = category ?: "",      // category임 plastic-bottle 같은 거, FireStore의 collection(recycle_howto)와 맵핑되어 있음
                uploadDt = date ?: ""           // 업로드 날짜 (yyyyMMddHHmmss, 년월일시분초)
            )

            // 이제 FireStore에 올리는 작업을 진행함
            val db = FirebaseFirestore.getInstance()
            db.collection("classify_result")
                .document()
                .set(classifyResultData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "데이터가 정상적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()  // 프래그먼트 뒤로가기
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "데이터 저장 중 에러가 발생 했습니다.", Toast.LENGTH_SHORT).show()
                    Log.d("FireStore", it.message.toString())
                    findNavController().popBackStack()  // 프래그먼트 뒤로가기
                }
        } // end of fun


        // '완료' 버튼 클릭 시, 동작 설명
        // 먼저 Storage에 UUID(무작위 ID) 값으로 사용자가 찍은 사진을 업로드 함. 그리고 URL을 받아옴
        // 그리고 사용자의 UID, 이미지 URL, 카테고리(plastic-bottle, glass-bottle)의 정보를 FireStore에 저장함.
        binding.buttonOK.setOnClickListener {
            // storage에 파일 업로드할 객체를 여기서 생성함
            val storage:FirebaseStorage = Firebase.storage
            val storageRef:StorageReference = storage.reference

            // 업로드할 이미지의 파일 경로를 생성함 Storage/upload_images_무작위id_image.png 로 저장될거임.
            val filePath = "upload_images/${UUID.randomUUID()}_image.png"

            // 위에서 생성한 경로에 해당하는 참조 객체(Ref)를 만듦. 이 imgRef로 Storage에 업로드하면 됨
            val imgRef:StorageReference = storageRef.child(filePath)

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

                            insertFireStore(imgURL)
                        }
                    }.addOnFailureListener{ exception ->
                        // url 다운로드 실패
                        Log.d("Storage", "Download URL Failed : ${exception.message}")
                    }
                } else { // 업로드 실패
                    Log.d("Storage", "upload failed..")
                }
            }   // end of Upload
        }
        return binding.root
    }
}