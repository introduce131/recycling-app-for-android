package com.loveprofessor.recyclingapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
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
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.loveprofessor.recyclingapp.STEP_COUNT_UPDATED") {
                val stepCount = intent.getIntExtra("step_count", 0)
                val todayStepCount = intent.getIntExtra("today_step_count", 0)

                // 걸음 수를 ReportHomeFragment로 전달
                val fragment = supportFragmentManager.findFragmentByTag("ReportHomeFragment") as? ReportHomeFragment
                fragment?.updateStepCount(todayStepCount)  // Fragment의 메서드를 호출해서 업데이트
            }
        }
    }

    // 권한 요청 코드
    private val REQUEST_CODE_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NavigationView 헤더의 뷰 설정
        val navHeaderName: TextView = binding.navView.getHeaderView(0).findViewById(R.id.nav_header_name)
        val navHeaderArea: TextView = binding.navView.getHeaderView(0).findViewById(R.id.nav_header_area)
        val navHeaderDay: TextView = binding.navView.getHeaderView(0).findViewById(R.id.nav_header_day)

        // 헤더의 텍스트 설정
        navHeaderName.text = MyApplication.userNickname
        navHeaderArea.text = "거주지 : ${MyApplication.userZone}"
        navHeaderDay.text = "재활용 요일 : ${MyApplication.userGarbageday}"

        // NavigationView 헤더 클릭 시 토스트 메시지
        binding.navView.getHeaderView(0).setOnClickListener {
            val intent = Intent(this, EditInfoActivity::class.java)  // 이동할 액티비티 클래스 지정
            startActivity(intent)
        }

        setSupportActionBar(binding.appBarMain.toolbar)

        /** requestPermissions는 비동기적으로 작동해서 지금 이따구로 코드 작성하면 신체활동 권한만 입력받고, 알림 권한을 확인할 수 있는 dialog창은 안뜨는 문제가 있음 **/
        // '활동' 권한이 부여되었는지 먼저 확인을 한다.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            startStepCounterService()   // 권한이 있으면 서비스를 시작
        } else { // 권한이 없으면 요청
            Toast.makeText(this, "신체활동 권한이 거부되었습니다. 설정에서 관련 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_CODE_PERMISSION)
        }

        // '알림' 권한이 부여되었는지 확인한다. 만약 알람 권한이 꺼져있으면 다시 받으면 됨
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "알림 권한이 거부되었습니다. 설정에서 관련 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_PERMISSION)
        }
        /** ..여기까지 문제.. **/

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

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 승인되면 서비스 시작
                startStepCounterService()
            } else {
                // 권한이 거부된 경우 사용자에게 안내
                Toast.makeText(this, "권한이 거부되었습니다. 이 앱은 걸음 수 추적 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
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
                        finish()    // 현재 액티비티 종료
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

    // 서비스 시작 메서드
    private fun startStepCounterService() {
        val serviceIntent = Intent(this, StepCounterService::class.java)
        startForegroundService(serviceIntent) // startForegroundService로 서비스 시작
    }
}
