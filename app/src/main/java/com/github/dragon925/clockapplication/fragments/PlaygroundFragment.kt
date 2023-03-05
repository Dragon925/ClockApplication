package com.github.dragon925.clockapplication.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.github.dragon925.clockapplication.ClockView.NumberType
import com.github.dragon925.clockapplication.databinding.FragmentPlaygroundBinding
import java.util.*
import kotlin.math.abs

class PlaygroundFragment : Fragment() {

    private lateinit var binding: FragmentPlaygroundBinding

    private var colorChangeOption: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaygroundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnBack.setOnClickListener { it.findNavController().navigateUp() }
            btnEnd.setOnClickListener { activity?.finish() }
            slideBorderWidth.apply {
                valueFrom = 0f
                valueTo = clock.borderMaxWidth
                value = clock.borderWidth
                addOnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        clock.borderWidth = value
                    }
                }
            }
            slideTimeZone.apply {
                value = (clock.timeZone.rawOffset / 3600000).toFloat()
                addOnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        val sign = if (value < 0) "-" else "+"
                        val offset = abs(value).toInt()
                        clock.timeZone = TimeZone.getTimeZone("GMT$sign$offset")
                    }
                }
            }
            switchNumbersType.apply {
                isChecked = clock.numberType == NumberType.ARABIC
                setOnCheckedChangeListener { _, isChecked ->
                    clock.numberType = if (isChecked) NumberType.ARABIC else NumberType.ROMAN
                }
            }
            switchNumbersVisibility.apply {
                isChecked = clock.hasNumbers
                setOnCheckedChangeListener { _, isChecked ->
                    clock.hasNumbers = isChecked
                }
            }
            switchDiamondsVisibility.apply {
                isChecked = clock.hasDiamonds
                setOnCheckedChangeListener { _, isChecked ->
                    clock.hasDiamonds = isChecked
                }
            }
            acChooseChangeColor.apply {
                setOnItemClickListener { _, _, position, _ ->
                    colorChangeOption = position
                    updateColorSliders(position)
                }
            }
            slideRedColor.addOnChangeListener { _, value, fromUser ->
                if (fromUser && colorChangeOption != -1) {
                    updateColor(
                        value.toInt(),
                        slideGreenColor.value.toInt(),
                        slideBlueColor.value.toInt()
                    )
                }
            }
            slideGreenColor.addOnChangeListener { _, value, fromUser ->
                if (fromUser && colorChangeOption != -1) {
                    updateColor(
                        slideRedColor.value.toInt(),
                        value.toInt(),
                        slideBlueColor.value.toInt()
                    )
                }
            }
            slideBlueColor.addOnChangeListener { _, value, fromUser ->
                if (fromUser && colorChangeOption != -1) {
                    updateColor(
                        slideRedColor.value.toInt(),
                        slideGreenColor.value.toInt(),
                        value.toInt()
                    )
                }
            }
            btnResetColors.setOnClickListener {
                clock.setClockColor(Color.BLACK)
                clock.clockFaceColor = Color.WHITE
                updateColorSliders(colorChangeOption)
            }
        }
    }

    private fun updateColor(red: Int, green: Int, blue: Int) {
        with(binding) {
            when(colorChangeOption) {
                0 -> clock.borderColor = Color.rgb(red, green, blue)
                1 -> clock.clockFaceColor = Color.rgb(red, green, blue)
                2 -> clock.numberColor = Color.rgb(red, green, blue)
                3 -> clock.diamondColor = Color.rgb(red, green, blue)
                4 -> clock.setClockHandColor(Color.rgb(red, green, blue))
                5 -> clock.secondHandColor = Color.rgb(red, green, blue)
                6 -> clock.minuteHandColor = Color.rgb(red, green, blue)
                7 -> clock.hourHandColor = Color.rgb(red, green, blue)
                else -> Unit
            }
        }
    }

    private fun updateColorSliders(position: Int) {
        with(binding) {
            val (red, green, blue) = when(position) {
                0 -> parseColor(clock.borderColor)
                1 -> parseColor(clock.clockFaceColor)
                2 -> parseColor(clock.numberColor)
                3 -> parseColor(clock.diamondColor)
                4 -> parseColor(Color.BLACK)
                5 -> parseColor(clock.secondHandColor)
                6 -> parseColor(clock.minuteHandColor)
                7 -> parseColor(clock.hourHandColor)
                else -> Triple(0, 0, 0)
            }

            slideRedColor.value = red.toFloat()
            slideGreenColor.value = green.toFloat()
            slideBlueColor.value = blue.toFloat()
        }
    }

    private fun parseColor(color: Int): Triple<Int, Int, Int> {
        return Triple(Color.red(color), Color.green(color), Color.blue(color))
    }
}