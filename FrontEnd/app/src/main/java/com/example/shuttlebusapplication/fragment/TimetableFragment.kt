package com.example.shuttlebusapplication.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.adapter.ShuttleAdapter
import com.example.shuttlebusapplication.databinding.FragmentTimetableBinding
import com.example.shuttlebusapplication.model.ShuttleSchedule
import com.example.shuttlebusapplication.AlarmReceiver
import com.example.shuttlebusapplication.repository.ShuttleRepository
import java.text.SimpleDateFormat
import java.util.*

class TimetableFragment : Fragment() {

    private var _binding: FragmentTimetableBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ShuttleAdapter
    private lateinit var headerContainer: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerContainer = binding.headerContainer

        binding.btnMenu.setOnClickListener {
            findNavController().navigate(R.id.mainMenuFragment)
        }

        // 초기 데이터 로드 (4번 노선)
        adapter = ShuttleAdapter(ShuttleRepository().getRoute4(), requireContext())
        binding.recyclerViewTimetable.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TimetableFragment.adapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            )
        }

        showHeader("special")
        updateSelectedTab(binding.tabBtnRoute4.id)

        binding.tabBtnRoute1.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute1())
            updateSelectedTab(it.id)
            showHeader("normal")
        }
        binding.tabBtnRoute2.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute2())
            updateSelectedTab(it.id)
            showHeader("normal")
        }
        binding.tabBtnRoute3.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute3())
            updateSelectedTab(it.id)
            showHeader("special")
        }
        binding.tabBtnRoute4.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute4())
            updateSelectedTab(it.id)
            showHeader("special")
        }
        binding.tabBtnRoute5.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute5())
            updateSelectedTab(it.id)
            showHeader("normal")
        }
    }

    private fun updateSelectedTab(selectedButtonId: Int) {
        val buttons = listOf(
            binding.tabBtnRoute1,
            binding.tabBtnRoute2,
            binding.tabBtnRoute3,
            binding.tabBtnRoute4,
            binding.tabBtnRoute5
        )
        buttons.forEach { btn ->
            val isSelected = btn.id == selectedButtonId
            btn.setBackgroundColor(
                resources.getColor(
                    if (isSelected) R.color.purple_500 else R.color.tab_unselected,
                    null
                )
            )
            btn.setTextColor(
                resources.getColor(
                    if (isSelected) R.color.white else R.color.black,
                    null
                )
            )
        }
    }

    private fun showHeader(type: String) {
        headerContainer.removeAllViews()
        val layoutRes = when (type) {
            "normal"  -> R.layout.header_normal
            "special" -> R.layout.header_special
            else      -> throw IllegalArgumentException("잘못된 헤더 타입")
        }
        val headerView = layoutInflater.inflate(layoutRes, headerContainer, false)
        headerContainer.addView(headerView)
    }

    /** 🚨 알림 예약 (출발 3분 전) */
    private fun scheduleAlarm(shuttle: ShuttleSchedule) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Calendar.getInstance()

        try {
            val departureDate = sdf.parse(shuttle.departureTime) ?: throw IllegalArgumentException()
            val cal = Calendar.getInstance().apply {
                time = departureDate
                set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
                add(Calendar.MINUTE, -3)
            }

            if (cal.before(now)) {
                Toast.makeText(context, "이미 지난 시간입니다.", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
                putExtra("shuttleName", shuttle.shuttleName)
                putExtra("departureTime", shuttle.departureTime)
            }
            val requestCode = (shuttle.shuttleName + shuttle.departureTime).hashCode()
            val pending = PendingIntent.getBroadcast(
                requireContext(), requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)

            Toast.makeText(context, "알림이 예약되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "시간 파싱 오류", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
