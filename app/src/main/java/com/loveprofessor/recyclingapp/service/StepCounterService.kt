package com.loveprofessor.recyclingapp.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.loveprofessor.recyclingapp.R
import java.util.Calendar

class StepCounterService : Service() {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepCount = 0
    private var todayStepCount = 0
    private var stepCounterListener: SensorEventListener? = null
    private val NOTIFICATION_CHANNEL_ID = "step_counter_service_channel"

    override fun onCreate() {
        super.onCreate()

        startForegroundService()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor != null) {
            stepCounterListener = object : SensorEventListener {
                private var lastSavedStepCount: Int = -1

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                        val prefs = getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
                        val currentStepCount = event.values[0].toInt()

                        // 첫 실행 시 초기화
                        if (lastSavedStepCount == -1) {
                            lastSavedStepCount = prefs.getInt("last_step_count", currentStepCount)
                        }

                        // 오늘의 걸음 수 계산
                        todayStepCount = currentStepCount - lastSavedStepCount

                        // 센서 값이 리셋된 경우 (기기 재부팅 등)
                        if (currentStepCount < lastSavedStepCount) {
                            todayStepCount = currentStepCount
                            lastSavedStepCount = 0
                        }

                        // SharedPreferences에 저장
                        with(prefs.edit()) {
                            putInt("step_count", currentStepCount)
                            putInt("today_step_count", todayStepCount)
                            putInt("last_step_count", lastSavedStepCount)
                            apply()
                        }

                        // Broadcast 전송
                        val intent = Intent("com.loveprofessor.walksensor.STEP_COUNT_UPDATED")
                        intent.putExtra("step_count", currentStepCount)
                        intent.putExtra("today_step_count", todayStepCount)
                        sendBroadcast(intent)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(
                stepCounterListener,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        } else {
            Toast.makeText(this, "이 디바이스에서 걸음 센서를 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
            stopSelf()
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                midnightAlarm()
            } else {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        } else {
            midnightAlarm()
        }
    }

    // 포그라운드 서비스를 시작함과 동시에 알림권한을 받았으면, 동시에 Nofitication을 띄울 수 있음.
    // (걸음 수 추적 중...) 이라는 알림을 보낼 예정
    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Step Counter Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
            Log.d("StepCounterService", "Notification channel created or already exists")
        }

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("ECOBIN")
            .setContentText("이 앱에서 걸음 수를 추적하고 있습니다.")
            .setSmallIcon(R.drawable.ic_cup)
            .build()

        notificationManager.notify(1, notification)
        startForeground(1, notification)
        Log.d("StepCounterService", "Service started in foreground")
    }

    private fun midnightAlarm() {
        Log.d("jwbaek", "midnightAlarm()")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MidnightAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 5)
        calendar.set(Calendar.MILLISECOND, 0)

        val timeMidnight = calendar.timeInMillis
        val currentTime = System.currentTimeMillis()
        val trigger = if (timeMidnight > currentTime) timeMidnight else timeMidnight + 24 * 60 * 60 * 1000

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger,
            pendingIntent
        )

        val handler = Handler(mainLooper)
        handler.postDelayed({
            midnightAlarm()
        }, 24 * 60 * 60 * 1000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stepCounterListener?.let { sensorManager.unregisterListener(it) }
    }
}
