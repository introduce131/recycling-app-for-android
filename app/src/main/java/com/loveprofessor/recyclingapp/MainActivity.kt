package com.loveprofessor.recyclingapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import android.view.Menu
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

        setSupportActionBar(binding.appBarMain.toolbar)

        // 권한이 부여되었는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            startStepCounterService()   // 권한이 있으면 서비스를 시작
        } else { // 권한이 없으면 요청
            Toast.makeText(this, "권한이 거부되었습니다. 설정에서 관련 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_CODE_PERMISSION)
        }

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
        registerReceiver(stepCountReceiver, filter)  // BroadcastReceiver 등록
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

    // 서비스 시작 메서드
    private fun startStepCounterService() {
        val serviceIntent = Intent(this, StepCounterService::class.java)
        startForegroundService(serviceIntent) // startForegroundService로 서비스 시작
    }
}
