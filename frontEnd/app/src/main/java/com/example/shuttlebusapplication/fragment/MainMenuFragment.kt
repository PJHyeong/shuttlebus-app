package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shuttlebusapplication.R

class MainMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)

        val navController = findNavController()

        // 클릭 이벤트 연결
        view.findViewById<LinearLayout>(R.id.Map).setOnClickListener {
            navController.navigate(R.id.mapFragment) //
        }
        view.findViewById<LinearLayout>(R.id.Schedule).setOnClickListener {
            navController.navigate(R.id.timetableFragment) //
        }
        view.findViewById<LinearLayout>(R.id.Option).setOnClickListener {
            navController.navigate(R.id.settingsFragment) //
        }
        view.findViewById<LinearLayout>(R.id.Notice).setOnClickListener {
            navController.navigate(R.id.noticeFragment) //
        }


        return view
    }
}
