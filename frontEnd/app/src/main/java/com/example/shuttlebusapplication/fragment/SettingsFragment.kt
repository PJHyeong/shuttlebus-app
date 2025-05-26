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
import android.widget.Toast
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shuttlebusapplication.AlarmReceiver
import com.example.shuttlebusapplication.LoginActivity
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.repository.ShuttleRepository

class SettingsFragment : Fragment() {

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

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        switchNotification = view.findViewById(R.id.switchNotification)
        val btnLogout: Button = view.findViewById(R.id.btnLogout)
        val btnMenu: View = view.findViewById(R.id.btnMenu)

        // 1) tvUserId 참조 (xml에 android:id="@+id/tvUserId")
        val tvUserId: TextView = view.findViewById(R.id.tvUserId)

        // 2) SharedPreferences에서 닉네임 읽기
        val loginPrefs = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val nickname = loginPrefs.getString("nickname", "닉네임 설정 필요")

        // 3) TextView에 세팅
        tvUserId.text = nickname

        // 메뉴 버튼
        btnMenu.setOnClickListener {
            findNavController().navigate(R.id.mainMenuFragment)
        }

        // 마스터 스위치 초기화
        switchNotification.isChecked = prefs.getBoolean(MASTER_SWITCH_KEY, true)
        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(MASTER_SWITCH_KEY, isChecked).apply()
            if (!isChecked) {
                cancelAllAlarms()
                clearAllAlarmPrefs()
                Toast.makeText(requireContext(), "모든 알림을 OFF 했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "알림 서비스를 시작합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 로그아웃 버튼
        btnLogout.setOnClickListener {
            // 로그인 관련 토큰만 삭제 (user_prefs에서)
            val loginPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            loginPrefs.edit()
                .remove("jwt_token")
                .remove("nickname")
                .apply()

            // 로그인 화면으로 이동 (기존 Activity 모두 종료)
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            Toast.makeText(requireContext(), "로그아웃 했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /** 앱에 예약된 모든 셔틀 알람을 취소 */
    private fun cancelAllAlarms() {
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
    }

    /** SharedPreferences에 남아있는 모든 알람 상태 키를 삭제 */
    private fun clearAllAlarmPrefs() {
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
    }
}
