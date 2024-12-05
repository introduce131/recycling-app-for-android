package com.loveprofessor.recyclingapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.loveprofessor.recyclingapp.databinding.FragmentInputNicknameBinding

class InputNicknameFragment : Fragment() {

    private lateinit var binding: FragmentInputNicknameBinding
    private lateinit var editTextNickName: EditText
    private lateinit var buttonNext: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflater가 layoutInflater임
        binding = FragmentInputNicknameBinding.inflate(inflater, container, false)
        editTextNickName = binding.editTextNickName
        buttonNext = binding.buttonNext

        editTextNickName.addTextChangedListener(object: TextWatcher {
            // 텍스트가 바뀌기 전 이벤트
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            // 텍스트가 바뀌었을 때 이벤트
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            // 텍스트가 바뀌고 난 뒤 이벤트
            @SuppressLint("ResourceAsColor")
            override fun afterTextChanged(p0: Editable?) {
                // EditText에 텍스트가 있으면 버튼을 활성화, 없으면 비활성화
                if (p0.isNullOrEmpty()) {
                    buttonNext.isEnabled = false  // 텍스트가 없으면 버튼 비활성화
                    buttonNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_enabled_false))
                } else {
                    buttonNext.isEnabled = true   // 텍스트가 있으면 버튼 활성화
                    buttonNext.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_enabled_true))
                }
            }
        })

        // '다음' 버튼 클릭 시, 다음 프래그먼트로 넘어감
        buttonNext.setOnClickListener {
            val nickName:String = editTextNickName.text.toString().trim()

            // 정규식에 부합하지 않으면 리스너에서 나간다
            if(!isNickName(nickName)) {
                editTextNickName.error = "닉네임은 2~12자의 영어, 숫자, 한글만 가능합니다."
                editTextNickName.requestFocus() // 닉네임 editText에 포커스
                return@setOnClickListener
            }

            /* MyApplication 클래스에 앱에서 지정한 닉네임 저장 */
            MyApplication.userNickname = nickName

            val nextFragment:Fragment = InputZoneFragment()  // 다음 프래그먼트를 저장

            // requireActivity() : 현재 프래그먼트가 속한 액티비티(InputActivity)를 말함
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

            // 커스텀 애니메이션 추가
            transaction.setCustomAnimations(
                R.anim.anim_slide_in_from_left_fade_in,
                R.anim.anim_fade_out
            )
            // FrameLayout에 지역 입력 프래그먼트(InputZoneFragment)로 교체
            transaction.replace(R.id.fragment_container, nextFragment)
            transaction.addToBackStack(null)    // 뒤로가기 버튼
            transaction.commit()
        }

        // 화면 아무데나 누르면 키보드 내려감
        binding.root.setOnClickListener{
            hideKeyboard()
            false
        }

        return binding.root
    } // end of onCreateView

    // 닉네임 유효성 검사 함수 isNickName, 결과 문자열 반환함
    private fun isNickName(nickName:String):Boolean {
        // ^[](문자열의 시작),
        // a-zA-Z(영어), 0-9(숫자), 가-힣(한글), ㄱ-ㅎ(한글 모음), ㅏ-ㅣ(한글 자음),
        // $(문자열의 끝)
        // 글자 자리수 : 2-12자 까지를 나타낸 정규표현식
        val pattern = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ]{2,12}$"

        // 닉네임 문자열이 정규식에 부합하는지?, matches 메서드는 Boolean을 return함
        return Regex(pattern).matches(nickName)
    }

    /** https://blog.yena.io/studynote/2017/12/16/Android-HideKeyboard.html **/
    private fun hideKeyboard() {
        activity?.currentFocus?.let { focusView ->
            val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputManager?.hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

}