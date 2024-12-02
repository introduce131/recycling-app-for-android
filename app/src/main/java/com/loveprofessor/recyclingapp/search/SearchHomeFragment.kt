package com.loveprofessor.recyclingapp.search

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.loveprofessor.recyclingapp.databinding.FragmentSearchHomeBinding
import com.loveprofessor.recyclingapp.BuildConfig
import com.loveprofessor.recyclingapp.R
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

interface FirebaseCallback {
    fun onSuccess(responseData: String)  // 성공적으로 데이터를 받았을 때 호출되는 메서드
    fun onError(errorMessage: String)  // 오류가 발생했을 때 호출되는 메서드
}

class SearchHomeFragment : Fragment() {
    private lateinit var binding: FragmentSearchHomeBinding
    private lateinit var mapView: MapView
    private var kakaoMap: KakaoMap? = null
    private val client = OkHttpClient()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager:LocationManager
    private var currentLat:Double = 0.0  // 내 현재 위치의 위도
    private var currentLng:Double = 0.0  // 내 현재 위치의 경도

    private val KAKAO_LOCAL_API_URL = "https://dapi.kakao.com/v2/local/search/keyword.json"
    private lateinit var receiptArray:JSONArray     // 전자영수증발급 배열
    private lateinit var tumblerArray:JSONArray     // 텀블러·다회용컵이용 배열
    private lateinit var ecoItemsArray:JSONArray    // 친환경상품구매 배열

    private var isFirebaseDataLoaded = false  // 데이터가 전부 다 로드 되었는지 확인하는 플래그
    private var currentLabel: Label? = null   // 현재 내가 클릭한 라벨을 이 currentLabel에 저장하려고 함

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchHomeBinding.inflate(inflater, container, false)
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 위치 권한 요청
        checkLocationPermission()

        callFirebaseFunction(BuildConfig.POINT_USAGE_URL, object : FirebaseCallback {
            override fun onSuccess(responseData: String) {
                // 요청 성공 시 처리 로직
                val jsonObj = JSONObject(responseData)
                receiptArray = jsonObj.getJSONArray("전자영수증발급")
                tumblerArray = jsonObj.getJSONArray("텀블러·다회용컵이용")
                ecoItemsArray = jsonObj.getJSONArray("친환경상품구매")

                Log.d("callFirebase", "1. 전자영수증 발급: $receiptArray")
                Log.d("callFirebase", "2. 텀블러·다회용컵이용: $tumblerArray")
                Log.d("callFirebase", "3. 친환경상품구매: $ecoItemsArray")

                isFirebaseDataLoaded = true
                if (kakaoMap != null) {
                    pinnedMyLocation()
                }
            }

            override fun onError(error: String) {
                // 요청 실패 시 처리 로직
                Log.e("callFirebase", "Error: $error")
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapView
        KakaoMapSdk.init(requireContext(), BuildConfig.KAKAO_MAP_KEY) // Kakao Map SDK 초기화

        // MapView를 시작하고 콜백을 설정
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("KakaoMap", "onMapDestroy()")
            }

            override fun onMapError(p0: Exception?) {
                if (p0 != null) {
                    Log.d("KakaoMap", "onMapError: ${p0.localizedMessage}")
                }
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(p0: KakaoMap) {
                kakaoMap = p0
                Log.d("KakaoMap", "1. onMapReady called")  // 정상적으로 호출되는지 로그 확인

                if (isFirebaseDataLoaded) {
                    // Firebase 데이터가 이미 로드된 상태라면, KakaoMap이 준비 완료되었으므로 showMapView를 호출
                    val cameraUpdate = CameraUpdateFactory.newCenterPosition(LatLng.from(currentLat, currentLng), 16)
                    kakaoMap?.moveCamera(cameraUpdate)
                    pinnedMyLocation()
                }
            }
        })

        // 전체 버튼 클릭 이벤트
        binding.imageButtonAll.setOnClickListener {
            if(isFirebaseDataLoaded) {
                val categories = listOf(
                    Pair(receiptArray, ""),
                    Pair(tumblerArray, ""),
                    Pair(ecoItemsArray, "")
                )

                // 각 카테고리 배열을 순차적으로 showMapView 함수에 전달함
                for((dataArray, category) in categories) {
                    showMapView(dataArray, category)
                }
            }
        }

        // 전자 영수증 버튼 클릭 이벤트
        binding.imageButtonReceipt.setOnClickListener {
            if(isFirebaseDataLoaded) {
                showMapView(receiptArray, "전자영수증")
            }
        }

        // 텀블러·다회용컵이용 버튼 클릭 이벤트
        binding.imageButtonTumbler.setOnClickListener {
            if(isFirebaseDataLoaded) {
                showMapView(tumblerArray, "텀블러·다회용컵")
            }
        }

        // 전자 영수증 버튼 클릭 이벤트
        binding.imageButtonEcoItems.setOnClickListener {
            if(isFirebaseDataLoaded) {
                showMapView(ecoItemsArray, "친환경상품")
            }
        }
    }

    // pinnedMyLocation() : 내 현재 위치에 파란색 핀을 찍어주는 메서드
    private fun pinnedMyLocation() {
        val cameraUpdate = CameraUpdateFactory.newCenterPosition(LatLng.from(currentLat, currentLng), 16)
        kakaoMap?.moveCamera(cameraUpdate)

        val labelStyle = LabelStyle.from(R.drawable.ic_my_pin)
            .setTextStyles(30, R.color.black)

        val labelOptions = LabelOptions.from(LatLng.from(currentLat, currentLng))
            .setStyles(labelStyle)
            .setClickable(true)
        kakaoMap?.labelManager?.layer?.addLabel(labelOptions)
    }

    // checkLocationPermission() : 위치(Location) 권한을 활성화 시켜주는 함수
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "위치 권한이 거부되었습니다. 설정에서 권한을 활성화 해주세요.", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        }
    }

    // getCurrentLocation() : LocationManager로 내 현재 위치를 가져와서 위도와 경도를 저장해주는 함수
    private fun getCurrentLocation() {
        try {
            // 위치 권한 check
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        location?.let { loc ->
                            currentLat = loc.latitude
                            currentLng = loc.longitude
                            Log.d("CurrentLocation", "lat : $currentLat lng : $currentLng")
                        } ?: run {
                            Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "위치 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(requireContext(), "위치 권한이 없습니다.", Toast.LENGTH_LONG).show()
            }
        } catch (exception:Exception) {
            Toast.makeText(requireContext(), "위치 권한이 없습니다.", Toast.LENGTH_LONG).show()
        }
    }

    // showMapView에서 데이터 가공 후 searchLocation() 함수로 이동해서 데이터를 뿌려줄거임
    // 앞에서 데이터 가져오는 부분 빼고 kakaoMap에서는 여기가 제일 먼저 시작되는 부분이라고 생각해도 됨.

    private val labelMap = mutableMapOf<String, LabelData>()
    private fun showMapView(dataArray: JSONArray, categoryName:String) {
        Log.d("KakaoMap", "0. 초기화 후 전자영수증 : $dataArray")  // 정상적으로 호출되는지 로그 확인

        // 맵에 표시된 기존 마커들을 전부 제거함
        kakaoMap?.labelManager?.layer?.removeAll()
        currentLabel = null

        // 카카오맵에서 현재 내 위치로 시점을 옮기고, 파란색 핀으로 내 위치를 찍어줌
        pinnedMyLocation()

        for(i in 0 until dataArray.length()) {
            val items = dataArray.getJSONObject(i)
            val head = items.getString("head")
            val branch = items.getString("branch")

            if(branch == "*") {
                Log.d("KakaoMap", "1. 전체 지점 리스트 ${head.trim()}")
                searchLocation(head, false, categoryName)
            } else {
                val branchList = branch.split(",")
                for(branchName in branchList) {
                    Log.d("KakaoMap", "1. 일부 지점 리스트 :  ${head.trim()} ${branchName.trim()}")
                    searchLocation("${head.trim()} ${branchName.trim()}", true, categoryName)
                }
            }
        }
    }

    // keyword(상호명)를 받아서 여기서 kakao의 local api를 사용해서 키워드에 대한 경도(lat)과 위도(lng)와 상호명(place_name)을 받아옴
    // 그리고 searchLocation() -> showOnMap() 함수를 호출해서 맵에 정확한 위치를 표시한다.
    private fun searchLocation(keyword: String, accuracy:Boolean, categoryName: String) {
        Thread {
            try {
                val radius = 3000 // 반경 3km
                Log.d("searchLocation", "1. 내 현재 위치는 Lat : $currentLat Lng : $currentLng")
                val request = Request.Builder()
                    .url("$KAKAO_LOCAL_API_URL?query=$keyword&x=$currentLng&y=$currentLat&radius=$radius&sort=accuracy")
                    .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}")
                    .build()


                // 요청에 대한 response(응답)를 동기적(.execute)로 받아옴
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("KakaoMap", "2. response Sucess")  // 정상적으로 호출되는지 로그 확인
                    val responseData = response.body?.string()
                    val jsonObject = JSONObject(responseData)
                    val documents = jsonObject.getJSONArray("documents")  // 응답 결과: documents:Document[]

                    // 상호명(head) 하나로 검색하면 이 로직을 타면 되고
                    if(!accuracy) {
                        for(i in 0 until documents.length()) {
                            val place = documents.getJSONObject(i)
                            val lat = place.getString("y").toDouble() // 위도
                            val lng = place.getString("x").toDouble() // 경도

                            Log.d("3-1. 전체 지점 object : ","$place")
                            Log.d("3-2. 전체 지점 정보: ", "head place: ${place.getString("place_name")}, x: $lat, y: $lng")

                            // 키워드가 맨 앞에 위치한 데이터만 필터링함
                            // "롯데아울렛 %" 로 하면 정확한 데이터를 얻어올 수 있음, "%롯데아울렛%"으로 검색하면 부정확한 상호명도 가져오기 때문에 이렇게 처리했음.
                            if(place.getString("place_name").startsWith(keyword)) {
                                showOnMap(lat, lng, place.getString("place_name"), categoryName)
                            }
                        }
                    } else { // 상호명(head)과 지점명(branch)를 검색하는거는 정확도를 위해 가장 상위 데이터 1개만 불러옴
                        if (documents.length() > 0) {
                            val place = documents.getJSONObject(0)
                            val lat = place.getString("y").toDouble() // 위도
                            val lng = place.getString("x").toDouble() // 경도

                            Log.d("4-1. 일부 지점 object : ","$place")
                            Log.d("4-2. 일부 지점 정보: ", "head place: ${place.getString("place_name")}, x: $lat, y: $lng")

                            showOnMap(lat, lng, place.getString("place_name"), categoryName)
                        }
                    }
                } else {
                    Log.e("SearchKeyword", "Error code: ${response.code}, message: ${response.body?.string()}")
                }
            } catch (exception: Exception) {
                Toast.makeText(requireContext(), "검색 실패", Toast.LENGTH_LONG).show()
                Log.e("SearchKeyword", "검색 실패: ${exception.message}")
            }
        }.start()
    }

    // Label 표시에 필요한 데이터를 data class로 정의해놨음
    // JsonObject로 하려고 했는데 data class가 더 관리하기 쉬울 거 같아서 이거로 함
    data class LabelData (
        val name:String,
        val lat: Double,
        val lng: Double
    )

    // 현재 활성화된 라벨을 다시 사라지게 처리하는 함수 resetCurrentLabel()
    // 활성화된 라벨(핀 + 텍스트)을 원래 라벨(핀)로 바꿔치면 되지 않을까
    private fun resetCurrentLabel() {
        currentLabel?.let { label ->
            kakaoMap?.let { map ->
                // label에 tag를 set하는 부분이 있는데 그곳에 data를 넣어놔서.. tag를 통해 data를 받아옴
                val data = label.tag as? LabelData
                if (data != null) {
                    val labelStyle = LabelStyle.from(R.drawable.ic_pin)
                        .setTextStyles(30, R.color.black)

                    val labelOptions = LabelOptions.from(LatLng.from(data.lat, data.lng))
                        .setStyles(labelStyle)
                        .setClickable(true)
                        .setTag(data)

                    map.labelManager?.layer?.remove(label)  // 여기서 현재 활성화된 label(currentLabel)을 레이어에서 지우고
                    // 그리고 원래의 핀 정보를 담고 있는 labelOptions를 바탕으로 추가하면 되지 않을까하는데 -> 되네 이게
                    map.labelManager?.layer?.addLabel(labelOptions)
                }
            }
        }
        currentLabel = null
    }
        private fun showOnMap(latitude: Double, longitude: Double, placeName: String, categoryName:String) {
            Log.d("KakaoMap", "3. showOnMap")  // 정상적으로 호출되는지 로그 확인
            kakaoMap?.let { map ->
                Log.d("KakaoMap", "4. valueCheck : x : ${latitude}, y : ${longitude}, place : $placeName")  // 정상적으로 호출되는지 로그 확인
                val position = LatLng.from(latitude, longitude)

                // 기본 라벨 스타일을 설정함 (일단 마커(Label)만 표시함)
                val labelStyle = LabelStyle.from(R.drawable.ic_pin)
                    .setTextStyles(30, R.color.black)

                val labelData = LabelData(placeName, latitude, longitude)

                val labelOptions = LabelOptions.from(position)
                    .setStyles(labelStyle)
                    .setClickable(true)
                    .setTag(labelData)

                map.labelManager?.layer?.addLabel(labelOptions)

                // pin(Label) 클릭 이벤트를 처리함.
                map.setOnLabelClickListener { _, layer, label ->
                    try {
                        // 만약, 현재 확장된 라벨이 있다면 원래 상태로 되돌리기 위해 호출함
                        if (currentLabel != null && currentLabel != label) {
                            resetCurrentLabel()
                        }

                        val data = label?.tag as LabelData

                        // 기존 라벨 스타일에 텍스트를 추가함
                        val labelStyle = LabelStyle.from(R.drawable.ic_pin)
                            .setTextStyles(30, R.color.black)

                        var category = ""

                        if(categoryName.isNotEmpty()) category = "[$categoryName]"
                        else category = ""

                        // 클릭된 라벨에 업데이트를 적용함
                        val labelOptions = LabelOptions.from(LatLng.from(data.lat, data.lng))
                            .setStyles(labelStyle)
                            .setTexts(data.name, "$category")
                            .setClickable(true)
                            .setTag(data)

                        layer?.remove(label)
                        val newLabel = layer?.addLabel(labelOptions)
                        currentLabel = newLabel
                    } catch (e: Exception) {
                        Log.e("Kakaomap", "error message : ${e.message}")
                    }
                }
            }
        }

    // Firebase Function 호출 함수
    private fun callFirebaseFunction(url: String, callback: FirebaseCallback) {
        // 비동기 처리로 HTTP 요청 보내기
        Thread {
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    Log.d("FirebaseFunction", "Response: $responseData")
                    callback.onSuccess(responseData ?: "")
                } else {
                    callback.onError(response.message)
                }
            } catch (e: Exception) {
                callback.onError("예외 발생: ${e.message}")
            }
        }.start()
    }
}
