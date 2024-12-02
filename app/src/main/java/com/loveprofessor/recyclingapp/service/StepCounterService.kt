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
                        val prefs = getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
                        val lastStepCount = prefs.getInt("last_step_count", 0)  // 어제까지 누적 걸음 수 데이터

                        stepCount = event.values[0].toInt() // 누적 걸음 수
                        todayStepCount = stepCount - lastStepCount  // 오늘 걸음 수 (누적걸음 - 어제까지 누적걸음)
                        // 걸음 수를 SharedPreferences에 저장
                        with(prefs.edit()) {
                            putInt("step_count", stepCount)
                            putInt("today_step_count", todayStepCount)
                            apply()
                        }

                        // 걸음 수가 갱신될 때(onSensorChanged)마다 Broadcast로 알림
                        val intent = Intent("com.loveprofessor.walksensor.STEP_COUNT_UPDATED")
                        intent.putExtra("step_count", stepCount)
                        intent.putExtra("today_step_count", todayStepCount)  // 오늘의 걸음 수도 전달
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
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // setExactAndAllowWhileIdle 메서드를 호출하려면 SCHEDULE_EXACT_ALARM 권한이 필요하다.
        // 근데 SCHEDULE_EXACT_ALARM 권한을 먼저 확인하고 요청하는 로직이 필요한데, 이 부분을 넣지 않으면 에디터가 찡찡댐
        // Android 12 (API 31) 이상에서 정확한 알람을 설정하려면 해당 권한이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                midnightAlarm()  // 권한이 있으면 자정에 알람을 설정
            } else {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)  // 권한이 없으면 시스템 설정 화면으로 이동
                startActivity(intent)
            }
        } else {
            midnightAlarm()  // Android 12 이전 버전에서는 권한 체크 없이 바로 알람을 설정
        }
    }

    //onStartCommand() : 서비스가 강제로 종료되었을 때 시스템이 서비스를 다시 시작하려고 시도함.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    // 자정에 AlarmManager를 통해 작업할 내용이 있음
    private fun midnightAlarm() {
        Log.d("jwbaek", "midnightAlarm()")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MidnightAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // 자정 00시 00분 05초로 설정
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 5)
        calendar.set(Calendar.MILLISECOND, 0)

        val timeMidnight = calendar.timeInMillis
        val currentTime = System.currentTimeMillis()

        // 만약, 자정(0시 00분)이 이미 지났다면, 내일 자정에 실행되도록 설정을 해줌
        val trigger = if (timeMidnight > currentTime) timeMidnight else timeMidnight + 24 * 60 * 60 * 1000

        // Doze 모드를 무시하고, 정확한 시간에 알람을 실행하도록 함
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger,
            pendingIntent
        )

        // setExactAndAllowWhileIdle는 Repeat이 안되니, 24시간 뒤에 다시 설정되도록 해야 한다.
        // 1. 메인스레드의 루퍼를 사용해서 handler 객체를 생성, 이 핸들러 객체는 이제 메인 스레드에서 실행
        val handler = Handler(mainLooper)

        // 2. postDelayed 메서드는 지정된 시간이 지난 후에 작업을 실행하는 메서드임.
        handler.postDelayed({
            midnightAlarm() // 3. 알람이 실행된 후, 다시 midnightAlarm()을 호출해서 알람을 재설정 한다. 24시간 뒤에
        }, 24 * 60 * 60 * 1000)

        // 매일 자정에 반복, 근데 문제가 있음 얘는 절전모드(Doze 모드)에서 작동하지 않을 수 있고, 테스트해보니까 진짜 작동 안하고 넘어가는 경우가 있어서 당황스러움.
        // 이 코드는 이제 사용하지 않음. (2024-11-29)
        /*
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            trigger,
            AlarmManager.INTERVAL_DAY, // 하루마다 실행
            pendingIntent
        )
        */
    }

    // 포그라운드 서비스로 시작하는 메서드
    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 포그라운드 서비스 알림 채널 설정 (Android 8.0 이상에서 필요)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Step Counter Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
            Log.d("StepCounterService", "Notification channel created or already exists")
        }

        // 알림 생성
        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("걸음 수 추적 중")
            .setContentText("걸음 수를 추적하고 있습니다.")
            .setSmallIcon(R.drawable.ic_cup) // 알림 아이콘 확인 필요
            .build()

        notificationManager.notify(1, notification)

        // 서비스가 포그라운드에서 실행되도록 설정
        startForeground(1, notification)
        Log.d("StepCounterService", "Service started in foreground")
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

