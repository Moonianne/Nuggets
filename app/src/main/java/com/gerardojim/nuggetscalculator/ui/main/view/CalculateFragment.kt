package com.gerardojim.nuggetscalculator.ui.main.view

import android.content.Context
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
import com.gerardojim.nuggetscalculator.ui.main.AndroidPreferences
import com.gerardojim.nuggetscalculator.ui.main.domain.FoodType
import com.gerardojim.nuggetscalculator.ui.main.exhaustive
import com.gerardojim.nuggetscalculator.ui.main.viewUtil.checkedChanges
import com.gerardojim.nuggetscalculator.ui.main.viewUtil.selectionChanges
import com.gerardojim.nuggetscalculator.ui.main.viewUtil.textChanges
import com.gerardojim.nuggetscalculator.ui.main.viewmodel.CalculateViewModel
import com.gerardojim.nuggetscalculator.ui.main.viewmodel.MainViewModelFactory
import com.gerardojim.nuggetscalculator.ui.main.viewstate.MainState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
        val prefs = AndroidPreferences(requireActivity().getPreferences(Context.MODE_PRIVATE))
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(prefs))[CalculateViewModel::class.java]
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
        observeViewModel()
    }

    private fun setupClicks(state: MainState) {

         /*
         TODO these interactions with the view result in a testable Intent but as is testing the
          an interaction with only one of these views is not possible.
          Considering other approaches
         */
        lifecycleScope.launch {
            binding.caloriesEditText.textChanges()
                .filterNot { it.isEmpty() }.map { it.toString().toInt() }.also {
                    state.onCaloricTargetInput.map { send -> it.collect(send) }
                }
            binding.foodDropdown.selectionChanges().also {
                state.onSelectedFoodTypeChanged.map { send -> it.map { FoodType.fromPosition(it) }.collect(send) }
            }
            binding.greenieSwitch.checkedChanges().also {
                state.onWithGreenieSwitched.map { send -> it.collect(send) }
            }

            binding.dryfoodSwitch.checkedChanges().collect { isChecked ->
                Log.d(null, "jimenez - test - dryFoodSwitch = $isChecked")
                state.onWithDryFoodSwitched.map { listener ->
                    listener.invoke(isChecked)
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is MainState.Error -> throw it.throwable
                    is MainState.Loading -> setupFoodPicker(it)
                    is MainState.NeedInput -> {
                        // TEMP
                    }
                    is MainState.Success -> {
                        binding.wetFoodTotal.text = it.mealServing.wetFoodServing.toString()
                        binding.dryFoodTotal.text = it.mealServing.dryFoodServing.toString()
                    }
                }.exhaustive
                setupFoodPicker(it)
                setupClicks(it)
            }
        }
    }

    private fun setupFoodPicker(it: MainState) {
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
