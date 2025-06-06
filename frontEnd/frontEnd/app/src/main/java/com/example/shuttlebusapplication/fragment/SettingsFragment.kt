// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/fragment/SettingsFragment.kt

package com.example.shuttlebusapplication.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shuttlebusapplication.AlarmReceiver
import com.example.shuttlebusapplication.LoginActivity
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.repository.ShuttleRepository

class SettingsFragment : Fragment() {

    // TimetableFragment 쪽 스케줄 알람용 SharedPreferences 이름
    private val PREFS_NAME = "AlarmPrefs"
    private val MASTER_SWITCH_KEY = "alarm_master_switch"

    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var switchNotification: Switch

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) TimetableFragment 쪽 SharedPreferences 초기화
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        switchNotification = view.findViewById(R.id.switchNotification)

        // 2) 사용자 닉네임 표시
        val tvUserId: TextView = view.findViewById(R.id.tvUserId)
        val loginPrefs = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val nickname = loginPrefs.getString("nickname", "닉네임 설정 필요")
        tvUserId.text = nickname

        // 3) 메뉴 버튼
        val btnMenu: View = view.findViewById(R.id.btnMenu)
        btnMenu.setOnClickListener {
            findNavController().navigate(R.id.mainMenuFragment)
        }

        // 4) 마스터 스위치 초기화
        switchNotification.isChecked = prefs.getBoolean(MASTER_SWITCH_KEY, true)
        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(MASTER_SWITCH_KEY, isChecked).apply()
            if (!isChecked) {
                // 마스터 OFF → 모든 알람 취소 및 키 삭제
                cancelAllAlarms()
                clearAllAlarmPrefs()
                Toast.makeText(requireContext(), "모든 알림을 OFF 했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "알림 서비스를 시작합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 5) 로그아웃 버튼
        val btnLogout: Button = view.findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            val appPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            appPrefs.edit().apply {
                remove("jwt_token")
                remove("nickname")
                remove("isAdmin")
                remove("auto_login_enabled")
                remove("saved_id")
                remove("saved_pw")
                apply()
            }
            val userPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            userPrefs.edit().apply {
                remove("jwt_token")
                remove("nickname")
                apply()
            }

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(requireContext(), "로그아웃 했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 앱에 예약된 모든 셔틀 알람을 취소
     * ① ShuttleRepository 기반 스케줄 알람
     * ② BottomSheetFragment(정류장 도착 알림) 기반 알람
     */
    private fun cancelAllAlarms() {
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ─── ① ShuttleRepository 쪽 스케줄 알람 취소 ───
        val repo = ShuttleRepository()
        val allSchedules = listOf(
            repo.getRoute1(),
            repo.getRoute2(),
            repo.getRoute3(),
            repo.getRoute4(),
            repo.getRoute5()
        ).flatten()

        for (item in allSchedules) {
            val key = item.departureTime + item.shuttleName
            val pi = PendingIntent.getBroadcast(
                requireContext(),
                key.hashCode(),
                Intent(requireContext(), AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        }

        // ─── ② BottomSheetFragment 쪽 알람 취소 ───
        // “alarm_prefs” SharedPreferences에서 저장된 모든 키(“stationIdx|shuttleName”)를 순회하여 취소
        val bottomPrefs = requireContext().getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        for (key in bottomPrefs.all.keys) {
            val parts = key.split("|", limit = 2)
            if (parts.size < 2) continue
            val stationIdx = parts[0].toIntOrNull() ?: continue
            val shuttleName = parts[1]

            val requestCode = stationIdx * 1000 + shuttleName.hashCode()
            val intent = Intent(requireContext(), AlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                requireContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        }
    }

    /**
     * SharedPreferences에 남아있는 모든 알람 상태 키를 삭제
     * ① ShuttleRepository 키 삭제
     * ② BottomSheetFragment("alarm_prefs") 키 삭제
     */
    private fun clearAllAlarmPrefs() {
        // ① TimetableFragment(ShuttleRepository)용 키 삭제
        val editor = prefs.edit()
        val repo = ShuttleRepository()
        val allSchedules = listOf(
            repo.getRoute1(),
            repo.getRoute2(),
            repo.getRoute3(),
            repo.getRoute4(),
            repo.getRoute5()
        ).flatten()

        for (item in allSchedules) {
            val key = item.departureTime + item.shuttleName
            editor.remove(key)
        }
        editor.apply()

        // ② BottomSheetFragment("alarm_prefs") 용 키 삭제
        val bottomPrefs = requireContext().getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val bottomEditor = bottomPrefs.edit()
        for (key in bottomPrefs.all.keys) {
            bottomEditor.remove(key)
        }
        bottomEditor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 뷰 바인딩 해제 등 불필요한 메모리 해제 작업이 있으면 여기에 작성
    }
}
