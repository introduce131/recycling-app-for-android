package com.loveprofessor.recyclingapp.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.loveprofessor.recyclingapp.databinding.FragmentSearchHomeBinding
import com.loveprofessor.recyclingapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.lang.Exception

class SearchHomeFragment : Fragment() {
    private lateinit var binding: FragmentSearchHomeBinding
    private lateinit var mapView: MapView
    private var kakaoMap : KakaoMap? = null
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentSearchHomeBinding.inflate(inflater, container, false)
        callFirebaseFunction(BuildConfig.POINT_USAGE_URL)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMapView()
    }

    private fun showMapView() {
        mapView = binding.mapView
        KakaoMapSdk.init(requireContext(), BuildConfig.KAKAO_MAP_KEY)  // Kakao Map SDK 초기화

        mapView.start(object: MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API가 정상적으로 죵로될 때 호출됨
                Log.d("KakaoMap", "onMapDestroy()")
            }

            override fun onMapError(p0: Exception?) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
                if (p0 != null) {
                    Log.d("KakaoMap", "onMapError : ${p0.localizedMessage}")
                }
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(p0: KakaoMap) {
                // 정상적으로 인증이 완료되었을 때 호출
                kakaoMap = p0
            }
        })
    }

    // URL 주소로 FirebaseFunction를 호출하는 함수 callFirebaseFunction
    private fun callFirebaseFunction(url: String) {
        // 비동기 처리로 HTTP 요청 보내기
        Thread {
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("FirebaseFunctions", "Response: $responseData")
                } else {
                    Log.e("FirebaseFunctions", "Error: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("FirebaseFunctions", "Request failed", e)
            }
        }.start()
    }
}