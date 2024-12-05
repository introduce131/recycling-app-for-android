package com.loveprofessor.recyclingapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import cn.pedant.SweetAlert.SweetAlertDialog
import com.loveprofessor.recyclingapp.databinding.ActivityMainBinding
import com.loveprofessor.recyclingapp.report.ReportHomeFragment
import com.loveprofessor.recyclingapp.service.StepCounterService

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    // BroadcastReceiver 등록하여 걸음 수를 실시간으로 받기
    private val stepCountReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent:    Intent) {
            if (intent.action == "com.loveprofessor.recyclingapp.STEP_COUNT_UPDATED") {
                val stepCount = intent.getIntExtra("step_count", 0)
                val todayStepCount = intent.getIntExtra("today_step_count", 0)

                // 걸음 수를 ReportHomeFragment로 전달
                val fragment = supportFragmentManager.findFragmentByTag("ReportHomeFragment") as? ReportHomeFragment
                fragment?.updateStepCount(todayStepCount)  // Fragment의 메서드를 호출해서 업데이트
            }
        }
    }

    companion object {
        const val REQUEST_CODE_ACTIVITY_RECOGNITION = 100
        const val REQUEST_CODE_POST_NOTIFICATIONS = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 매 로그인 시 uId에 로그인 성공시에 가져온 uId 값을 저장함, 로그아웃할 때, remove할 예정
        val prefs = this.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        prefs.edit().putString("uId", MyApplication.uId).apply()

        Log.d("jwbaek", "main uid : ${MyApplication.uId}")

        // NavigationView 헤더의 뷰 설정
        val navHeaderName: TextView = binding.navView.getHeaderView(0).findViewById(R.id.nav_header_name)
        val navHeaderArea: TextView = binding.navView.getHeaderView(0).findViewById(R.id.nav_header_area)
        val navHeaderDay: TextView = binding.navView.getHeaderView(0).findViewById(R.id.nav_header_day)

        val userGarbageday = MyApplication.userGarbageday

        // 각 요일을 축약한 값으로 변환 "목요일" -> "목"
        val convertDays = userGarbageday
            .replace("월요일", "월")
            .replace("화요일", "화")
            .replace("수요일", "수")
            .replace("목요일", "목")
            .replace("금요일", "금")
            .replace("토요일", "토")
            .replace("일요일", "일")

        // 헤더의 텍스트 설정
        navHeaderName.text = MyApplication.userNickname
        navHeaderArea.text = "거주지 : ${MyApplication.userZone}"
        navHeaderDay.text = "재활용 요일 : $convertDays"

        // NavigationView 헤더 클릭 시 토스트 메시지
        binding.navView.getHeaderView(0).setOnClickListener {
            val intent = Intent(this, EditInfoActivity::class.java)  // 이동할 액티비티 클래스 지정
            startActivity(intent)
        }

        setSupportActionBar(binding.appBarMain.toolbar)

        /** 권한 입력 **/
        requestPermissions()

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_camera, R.id.nav_report, R.id.nav_Faq, R.id.nav_search
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("com.loveprofessor.recyclingapp.STEP_COUNT_UPDATED")
        registerReceiver(stepCountReceiver, filter, Context.RECEIVER_EXPORTED)  // BroadcastReceiver 등록
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(stepCountReceiver)  // BroadcastReceiver 해제
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /** 권한 요청 결과 처리 **/
    /** 참조 : https://ogyong.tistory.com/5 **/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_ACTIVITY_RECOGNITION -> {
                // 신체활동 권한 결과 처리
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "신체활동 권한이 승인되었습니다.")
                    startStepCounterService()  // 권한이 승인되면 서비스를 시작
                } else {
                    Log.d("Permission", "신체활동 권한이 거부되었습니다.")
                    Toast.makeText(this, "신체활동 권한이 필요합니다. 설정에서 활성화 해주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_CODE_POST_NOTIFICATIONS -> {
                // 알림 권한 결과 처리
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "알림 권한이 승인되었습니다.")
                } else {
                    Log.d("Permission", "알림 권한이 거부되었습니다.")
                    Toast.makeText(this, "알림 권한이 필요합니다. 설정에서 활성화 해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 메인 Activity에서 메뉴 선택 화면
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            // 앱 종료 (그러나 백그라운드 서비스는 종료되면 안된다)
            R.id.action_exit -> {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setContentText("앱을 종료 하시겠습니까?")
                    .setConfirmText("확인")
                    .setCancelText("취소")
                    .setCancelClickListener { dialog ->
                        dialog.dismiss()
                    }
                    .setConfirmClickListener {
                        finishAffinity()    // 앱을 종료하긴 하는데 백그라운드에서 도는 서비스는 끄지 않음.
                    }
                    .show()
                true
            }
            // 로그아웃
            R.id.action_logout -> {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setContentText("로그아웃 하시겠습니까?")
                    .setConfirmText("확인")
                    .setCancelText("취소")
                    .setCancelClickListener { dialog ->
                        dialog.dismiss()
                    }
                    .setConfirmClickListener {
                        MyApplication.userInfoReset()   // 로그인된 사용자 정보 초기화

                        // 그리고 SharedPreferences에 있는 uId를 삭제함
                        val prefs = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                        prefs.edit().remove("uId").apply()
                        finish()  // 현재 액티비티 종료
                    }
                    .show()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
                false
            }
        }
    }

    // Android 13 이상에서 알림 권한 요청
    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // 신체활동 권한이 없으면 추가
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        // 알림 권한이 없으면 추가
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 요청할 권한이 있다면 한 번에 요청
        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissions.launch(permissionsToRequest.toTypedArray())
        } else {
            // 이미 모든 권한이 승인된 경우
            startStepCounterService()
        }
    }

    // 다중 권한 요청
    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val activityRecognitionGranted = permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        val postNotificationsGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false

        if (activityRecognitionGranted && postNotificationsGranted) {
            Log.d("Permission", "두 권한이 모두 승인되었습니다.")
            startStepCounterService() // 권한이 승인되면 서비스 시작
        } else {
            Log.d("Permission", "필요한 권한이 거부되었습니다.")
            Toast.makeText(this, "모든 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // 서비스 시작 메서드
    private fun startStepCounterService() {
        val serviceIntent = Intent(this, StepCounterService::class.java)
        startForegroundService(serviceIntent) // startForegroundService로 서비스 시작
    }
}
