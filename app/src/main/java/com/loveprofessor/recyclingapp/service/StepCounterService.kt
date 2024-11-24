package com.loveprofessor.recyclingapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.app.NotificationCompat
import com.loveprofessor.recyclingapp.R

class StepCounterService : Service() {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepCount = 0
    private var stepCounterListener: SensorEventListener? = null

    private val NOTIFICATION_CHANNEL_ID = "step_counter_service_channel"

    override fun onCreate() {
        super.onCreate()

        // 포그라운드 서비스 시작
        startForegroundService()

        // 센서 매니저 초기화
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // 센서가 존재할 경우 이벤트 리스너 등록
        if (stepCounterSensor != null) {
            stepCounterListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                        stepCount = event.values[0].toInt()

                        // 걸음 수를 SharedPreferences에 저장
                        val prefs = getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putInt("step_count", stepCount).apply()

                        // 걸음 수가 갱신될 때마다 Broadcast로 알림
                        val intent = Intent("com.loveprofessor.walksensor.STEP_COUNT_UPDATED")
                        intent.putExtra("step_count", stepCount)
                        sendBroadcast(intent)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            // 센서 리스너 등록
            sensorManager.registerListener(stepCounterListener, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            Toast.makeText(this, "이 디바이스에서 걸음 센서를 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
            stopSelf()  // 센서가 없으면 서비스 종료
        }
    }

    // 포그라운드 서비스로 시작하는 메서드
    private fun startForegroundService() {
        // 포그라운드 서비스 알림 채널 설정 (Android 8.0 이상에서 필요)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Step Counter Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("걸음 수 추적 중")
            .setContentText("걸음 수를 추적하고 있습니다.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 알림 아이콘 확인 필요
            .build()

        // 서비스가 포그라운드에서 실행되도록 설정
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // 서비스 종료 시 센서 리스너 해제
        sensorManager.unregisterListener(stepCounterListener)
    }
}

