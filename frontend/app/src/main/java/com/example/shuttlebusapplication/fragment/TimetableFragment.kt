package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shuttlebusapplication.R
import com.example.shuttlebusapplication.adapter.ShuttleAdapter
import com.example.shuttlebusapplication.databinding.FragmentTimetableBinding
import com.example.shuttlebusapplication.repository.ShuttleRepository

class TimetableFragment : Fragment() {

    private var _binding: FragmentTimetableBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ShuttleAdapter

    // ✅ 헤더 뷰를 담을 ViewGroup
    private lateinit var headerContainer: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 헤더 컨테이너 초기화
        headerContainer = binding.headerContainer

        // ✅ 메뉴 이동 기능
        binding.btnMenu.setOnClickListener {
            findNavController().navigate(R.id.mainMenuFragment)
        }

        // ✅ 초기 데이터 (기본: Route4)
        val initialData = ShuttleRepository().getRoute4()
        adapter = ShuttleAdapter(initialData)

        binding.recyclerViewTimetable.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TimetableFragment.adapter
            // ✅ 구분선 추가
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                )
            )
        }

        // ✅ 초기 헤더
        showHeader("special")

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

        // ✅ 초기 선택 탭 (Route4)
        updateSelectedTab(binding.tabBtnRoute4.id)

        // ✅ 탭 클릭 이벤트
        binding.tabBtnRoute1.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute1())
            updateSelectedTab(binding.tabBtnRoute1.id)
            showHeader("normal")
        }

        binding.tabBtnRoute2.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute2())
            updateSelectedTab(binding.tabBtnRoute2.id)
            showHeader("normal")
        }

        binding.tabBtnRoute3.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute3())
            updateSelectedTab(binding.tabBtnRoute3.id)
            showHeader("special")
        }

        binding.tabBtnRoute4.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute4())
            updateSelectedTab(binding.tabBtnRoute4.id)
            showHeader("special")
        }

        binding.tabBtnRoute5.setOnClickListener {
            adapter.updateData(ShuttleRepository().getRoute5())
            updateSelectedTab(binding.tabBtnRoute5.id)
            showHeader("normal")
        }
    }

    // ✅ 헤더 보여주기 함수
    private fun showHeader(type: String) {
        headerContainer.removeAllViews()

        val headerLayout = when (type) {
            "normal" -> R.layout.header_normal
            "special" -> R.layout.header_special
            else -> throw IllegalArgumentException("Invalid header type")
        }

        val headerView = layoutInflater.inflate(headerLayout, headerContainer, false)
        headerContainer.addView(headerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
