package com.gerardojim.nuggetscalculator.ui.main.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.gerardojim.nuggetscalculator.databinding.MainFragmentBinding
import com.gerardojim.nuggetscalculator.ui.main.intent.MainIntent
import com.gerardojim.nuggetscalculator.ui.main.viewmodel.MainViewModel
import com.gerardojim.nuggetscalculator.ui.main.viewmodel.MainViewModelFactory
import com.gerardojim.nuggetscalculator.ui.main.viewstate.MainState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CalculateFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding

    companion object {
        fun newInstance() = CalculateFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, MainViewModelFactory()).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = MainFragmentBinding.inflate(inflater, container, false).let {
        binding = it
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClicks()
        observeViewModel()
    }

    private fun setupClicks() {
        binding.calculateButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.userIntent.send(
                    MainIntent.Calculate(
                        selectedFoodPosition = binding.foodPicker.selectedItemPosition,
                        dailyCaloricTarget = binding.caloriesEditText.text.toString().toInt(),
                        hasDryFood = binding.dryfoodSwitch.isChecked,
                        hasGreenie = binding.greenieSwitch.isChecked,
                    )
                )
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is MainState.Error -> throw it.throwable
                    is MainState.Init -> setupFoodPicker(it)
                    is MainState.Working -> {
                        // TEMP
                    }
                    is MainState.Results -> {
                        binding.resultMessage.apply {
                            text = """Pixel can have ${it.mealServing.wetFoodServing} grams of 
                                |wet food and ${it.mealServing.dryFoodServing} of dry food."""
                                .trimMargin()
                            visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun setupFoodPicker(it: MainState.Init) {
        binding.foodPicker.apply {
            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                it.foodTypes.map { foodType -> foodType.key })
            onItemSelectedListener = SimpleOnItemSelectedListener { position ->
                lifecycleScope.launch {
                    viewModel.userIntent.send(MainIntent.SelectFood(position))
                }
            }
        }
    }
}
