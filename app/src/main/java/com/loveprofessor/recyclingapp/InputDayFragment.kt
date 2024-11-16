package com.loveprofessor.recyclingapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.loveprofessor.recyclingapp.databinding.FragmentInputDayBinding

/* fireStore에 저장할 userData 형식 */
data class UserData (
    var userEmail:String = "",
    var userName:String = "",
    var userNickname:String = "",
    var userZone:String = "",
    var userGarbageday:String = "",
)

class InputDayFragment : Fragment() {
    private lateinit var binding: FragmentInputDayBinding
    private lateinit var buttonNext: Button
    private lateinit var dayCheckBoxes: Array<CheckBox>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInputDayBinding.inflate(inflater, container, false)
        buttonNext = binding.buttonNext

        // 요일 체크박스 array
        dayCheckBoxes = arrayOf(
            binding.checkBoxMon, binding.checkBoxTue, binding.checkBoxWed,
            binding.checkBoxThu, binding.checkBoxFri, binding.checkBoxSat, binding.checkBoxSat
        )

        // 실시간으로 버튼을 활성화시킬지 감지 시켜야함
        dayCheckBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener(object:CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    setButtonEnabled()
                }
            })
        }

        // 이제 홈 화면인 MainActivity로 이동
        buttonNext.setOnClickListener {
            var checkedDays = mutableListOf<String>()
            val nextFragment:Fragment = FinishFragment()  // 다음 프래그먼트를 저장

            // requireActivity() : 현재 프래그먼트가 속한 액티비티(InputActivity)를 말함
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

            // 커스텀 애니메이션 추가
            transaction.setCustomAnimations(
                R.anim.anim_slide_in_from_left_fade_in,
                R.anim.anim_fade_out
            )

            // 체크된 항목들만 저장
            dayCheckBoxes.forEach { checkBox ->
                if(checkBox.isChecked) checkedDays.add(checkBox.text.toString())
            }
            val result = checkedDays.joinToString(",")  // List를 하나의 문자열로 반환해주는 joinToString, 예)"월요일,화요일,목요일,금요일"

            /* MyApplication에 쓰레기 버리는 날을 저장한다. 예)"월요일,화요일,목요일,금요일" */
            MyApplication.userGarbageday = result

            var userData = UserData(
                userEmail = MyApplication.userEmail,
                userName = MyApplication.userName,
                userNickname = MyApplication.userNickname,
                userZone = MyApplication.userZone,
                userGarbageday = MyApplication.userGarbageday,
            )

            /* 이제 Firestore에 MyApplication에 저장된 데이터를 users collection에 저장할거임 */
            var db:FirebaseFirestore = FirebaseFirestore.getInstance()

            db.collection("users")
                .document(MyApplication.uId)
                .set(userData)
                .addOnSuccessListener {
                    // FrameLayout에 회원가입 완료 프래그먼트(FinishFragment)로 교체
                    transaction.replace(R.id.fragment_container, nextFragment)
                    transaction.addToBackStack(null)    // 뒤로가기 버튼
                    transaction.commit()
                }
                .addOnFailureListener{ e ->
                    Log.e("FireStore", "Error message : ${e.localizedMessage}")
                    Toast.makeText(requireContext(), "회원가입 중 데이터 저장에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show()
                }
        }

        return binding.root
    }

    // 체크박스 중 하나라도 isChecked가 true라면 true, 전부 false일때는 false임 any 함수에 의해서
    private fun setButtonEnabled() {
        val enabled = dayCheckBoxes.any { checkBox -> checkBox.isChecked } // any : 조건에 맞는 원소 하나라도 체크되어 있으면 무조건 true임
        buttonNext.isEnabled = enabled

        // 활성화/비활성화에 따라 버튼의 색상을 바꿔주자
        if(enabled) {
            buttonNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_enabled_true))
        } else {
            buttonNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_enabled_false))
        }
    }
}