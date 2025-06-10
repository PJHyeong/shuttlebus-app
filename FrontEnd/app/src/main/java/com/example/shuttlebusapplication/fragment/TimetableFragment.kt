// 파일 경로: app/src/main/java/com/example/shuttlebusapplication/fragment/TimetableFragment.kt

package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.*
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ─ (1) “메뉴” 버튼 클릭 → 메인 메뉴로 이동
        binding.btnMenu.setOnClickListener {
            findNavController().navigate(R.id.mainMenuFragment)
        }

        // ─ (2) ShuttleAdapter 초기화
        //     이제 onAlarmToggle 파라미터는 없으므로, data와 context만 넘깁니다.
        adapter = ShuttleAdapter(
            data = ShuttleRepository().getRoute4(),
            context = requireContext()
        )

        // ─ (3) 리사이클러뷰 세팅
        binding.recyclerViewTimetable.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TimetableFragment.adapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            )
        }

        // ─ (4) 기본 헤더/탭 초기화 (4번 노선이 special 타입)
        showHeader("special")
        updateSelectedTab(binding.tabBtnRoute4.id)

        // ─ (5) 탭 클릭 시 데이터 및 헤더/스타일 변경
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

    /**
     * 선택된 탭 버튼의 배경색과 글자색을 변경합니다.
     */
    private fun updateSelectedTab(selectedButtonId: Int) {
        val buttons = listOf(
            binding.tabBtnRoute1,
            binding.tabBtnRoute2,
            binding.tabBtnRoute3,
            binding.tabBtnRoute4,
            binding.tabBtnRoute5
        )
        buttons.forEach { btn ->
            val isSelected = (btn.id == selectedButtonId)
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

    /**
     * “normal” 또는 “special” 헤더 레이아웃으로 교체합니다.
     */
    private fun showHeader(type: String) {
        binding.headerContainer.removeAllViews()
        val layoutRes = when (type) {
            "normal"  -> R.layout.header_normal
            "special" -> R.layout.header_special
            else      -> throw IllegalArgumentException("잘못된 헤더 타입")
        }
        val headerView = layoutInflater.inflate(layoutRes, binding.headerContainer, false)
        binding.headerContainer.addView(headerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
