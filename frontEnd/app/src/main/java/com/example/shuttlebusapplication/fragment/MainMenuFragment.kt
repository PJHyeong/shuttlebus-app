package com.example.shuttlebusapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        view.findViewById<ImageView>(R.id.Map).setOnClickListener {
            navController.navigate(R.id.mapFragment) //
        }
        view.findViewById<ImageView>(R.id.Schedule).setOnClickListener {
            navController.navigate(R.id.timetableFragment) //
        }
        view.findViewById<ImageView>(R.id.Option).setOnClickListener {
            navController.navigate(R.id.settingsFragment) //
        }
        view.findViewById<ImageView>(R.id.Notice).setOnClickListener {
            navController.navigate(R.id.noticeFragment) //
        }


        return view
    }
}
