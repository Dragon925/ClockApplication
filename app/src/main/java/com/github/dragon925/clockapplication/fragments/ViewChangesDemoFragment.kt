package com.github.dragon925.clockapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.github.dragon925.clockapplication.R
import com.github.dragon925.clockapplication.databinding.FragmentViewChangesDemoBinding

class ViewChangesDemoFragment : Fragment() {

    private lateinit var binding: FragmentViewChangesDemoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewChangesDemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { it.findNavController().navigateUp() }
        binding.btnNext.setOnClickListener {
            it.findNavController().navigate(R.id.action_viewChangesDemoFragment_to_playgroundFragment)
        }
    }
}