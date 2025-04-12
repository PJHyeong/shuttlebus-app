package com.example.shuttlebusapplication.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.shuttlebusapplication.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private val PREFS_NAME = "AlarmPrefs"
    private val MASTER_SWITCH_KEY = "alarm_master_switch"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<LinearLayout>(R.id.btnBack)
        val switchNotification = view.findViewById<Switch>(R.id.switchNotification)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, 0)

        // ⏬ 마스터 스위치 상태 로드
        val isNotificationOn = prefs.getBoolean(MASTER_SWITCH_KEY, true)
        switchNotification.isChecked = isNotificationOn

        // ⬅ 뒤로가기 버튼
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 🔔 스위치 토글 시 전체 알림 마스터 상태 저장
        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(MASTER_SWITCH_KEY, isChecked).apply()

            val message = if (isChecked) {
                "알림 서비스를 시작합니다."
            } else {
                "알림 서비스를 종료합니다."
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // 🚪 로그아웃 버튼
        btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "로그아웃 합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}