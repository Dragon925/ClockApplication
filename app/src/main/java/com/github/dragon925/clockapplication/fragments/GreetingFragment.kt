package com.github.dragon925.clockapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.github.dragon925.clockapplication.R
import com.github.dragon925.clockapplication.databinding.FragmentGreetingBinding

class GreetingFragment : Fragment() {

    private lateinit var binding: FragmentGreetingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGreetingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnStart.setOnClickListener {
            it.findNavController().navigate(R.id.action_greetingFragment_to_timeZoneDemoFragment)
        }
    }

}