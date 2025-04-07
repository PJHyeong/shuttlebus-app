package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController // ✅ 추가!
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shuttlebusapplication.adapter.ShuttleAdapter
import com.example.shuttlebusapplication.databinding.FragmentTimetableBinding
import com.example.shuttlebusapplication.repository.ShuttleRepository
import com.example.shuttlebusapplication.R

class TimetableFragment : Fragment() {

    private var _binding: FragmentTimetableBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ShuttleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 메뉴 이동 기능 추가
        binding.btnMenu.setOnClickListener {
            findNavController().navigate(R.id.mainMenuFragment)
        }

        // 초기 데이터 (기본: Route4)
        val initialData = ShuttleRepository().getRoute4()
        adapter = ShuttleAdapter(initialData)

        binding.recyclerViewTimetable.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTimetable.adapter = adapter

        // ✅ 선택된 탭 강조 함수
        fun updateSelectedTab(selectedButtonId: Int) {
            val tabButtons = listOf(
                binding.tabBtnRoute1,
                binding.tabBtnRoute2,
                binding.tabBtnRoute3,
                binding.tabBtnRoute4,
                binding.tabBtnRoute5
            )

            tabButtons.forEach { button ->
                if (button.id == selectedButtonId) {
                    button.setBackgroundColor(resources.getColor(R.color.purple_500, null))
                    button.setTextColor(resources.getColor(R.color.white, null))
                } else {
                    button.setBackgroundColor(resources.getColor(R.color.tab_unselected, null))
                    button.setTextColor(resources.getColor(R.color.black, null))
                }
            }
        }

        // 앱 실행 시 초기 선택 탭 (Route4)
        updateSelectedTab(binding.tabBtnRoute4.id)

        // 탭 클릭 이벤트: 데이터 업데이트 + 탭 강조
        binding.tabBtnRoute1.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute1())
            updateSelectedTab(binding.tabBtnRoute1.id)
        }

        binding.tabBtnRoute2.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute2())
            updateSelectedTab(binding.tabBtnRoute2.id)
        }

        binding.tabBtnRoute3.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute3())
            updateSelectedTab(binding.tabBtnRoute3.id)
        }

        binding.tabBtnRoute4.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute4())
            updateSelectedTab(binding.tabBtnRoute4.id)
        }

        binding.tabBtnRoute5.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute5())
            updateSelectedTab(binding.tabBtnRoute5.id)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
