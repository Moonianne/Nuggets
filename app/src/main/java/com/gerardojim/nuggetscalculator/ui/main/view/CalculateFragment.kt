package com.gerardojim.nuggetscalculator.ui.main.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.gerardojim.nuggetscalculator.R
import com.gerardojim.nuggetscalculator.databinding.CalculateFragmentBinding
import com.gerardojim.nuggetscalculator.ui.main.intent.MainIntent
import com.gerardojim.nuggetscalculator.ui.main.viewUtil.checkedChanges
import com.gerardojim.nuggetscalculator.ui.main.viewUtil.selectionChanges
import com.gerardojim.nuggetscalculator.ui.main.viewUtil.textChanges
import com.gerardojim.nuggetscalculator.ui.main.viewmodel.CalculateViewModel
import com.gerardojim.nuggetscalculator.ui.main.viewmodel.MainViewModelFactory
import com.gerardojim.nuggetscalculator.ui.main.viewstate.MainState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class CalculateFragment : Fragment() {
    private lateinit var viewModel: CalculateViewModel
    private lateinit var binding: CalculateFragmentBinding

    companion object {
        fun newInstance() = CalculateFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(this, MainViewModelFactory()).get(CalculateViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = CalculateFragmentBinding.inflate(inflater, container, false).let {
        binding = it
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        observeViewModel()
    }

    private fun setupClicks() {

         /*
         TODO these interactions with the view result in a testable Intent but as is testing the
          an interaction with only one of these views is not possible.
          Considering other approaches
         */
        lifecycleScope.launch {
            val inputCaloriesSource = binding.caloriesEditText.textChanges()
                .filterNot { it.isEmpty() }.map { it.toString().toInt() }
            val selectedFoodSource = binding.foodDropdown.selectionChanges()
            val hasGreenieSource = binding.greenieSwitch.checkedChanges()
            val hasDryFoodSource = binding.dryfoodSwitch.checkedChanges()

            combine(
                inputCaloriesSource,
                selectedFoodSource,
                hasGreenieSource,
                hasDryFoodSource,
            ) { calories, food, hasGreenie, hasDryFood ->
                Log.d(null, "jimenez - test - $calories")
                MainIntent.Calculate(
                    selectedFoodPosition = food,
                    dailyCaloricTarget = calories,
                    hasDryFood = hasDryFood,
                    hasGreenie = hasGreenie,
                )
            }.collect { viewModel.userIntent.send(it) }
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
                        binding.wetFoodTotal.text = it.mealServing.wetFoodServing.toString()
                        binding.dryFoodTotal.text = it.mealServing.dryFoodServing.toString()
                    }
                }
            }
        }
    }

    private fun setupFoodPicker(it: MainState.Init) {
        binding.foodDropdown.apply {
            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                it.foodTypes.map { foodType -> foodType.key }
            )
            setAdapter(adapter)
        }
    }
}
