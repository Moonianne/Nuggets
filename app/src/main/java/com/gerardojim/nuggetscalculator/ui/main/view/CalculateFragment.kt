package com.gerardojim.nuggetscalculator.ui.main.view

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import kotlinx.coroutines.flow.* // ktlint-disable no-wildcard-imports
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

    private fun observeViewModel() {
        fun setupUserInteraction(mainState: MainState) {
            /*
         TODO these interactions with the view result in a testable Intent but as is testing the
          an interaction with only one of these views is not possible.
          Considering other approaches
         */
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        binding.foodDropdown.selectionChanges().collect { position ->
                            mainState.onSelectedFoodTypeChanged.map {
                                it(FoodType.fromPosition(position))
                            }
                        }
                    }
                    launch {
                        binding.dryfoodSwitch.checkedChanges().collect { isChecked ->
                            mainState.onWithDryFoodSwitched.map { it(isChecked) }
                        }
                    }
                    launch {
                        binding.caloriesEditText.textChanges()
                            .filterNot { it.isEmpty() }.map { it.toString().toInt() }
                            .collect { calorieTarget ->
                                mainState.onCaloricTargetInput.map { it(calorieTarget) }
                            }
                    }
                    launch {
                        binding.greenieSwitch.checkedChanges().collect { isChecked ->
                            mainState.onWithGreenieSwitched.map { it(isChecked) }
                        }
                    }
                }
            }
        }

        fun populateViews(it: MainState) {
            it.caloricTarget.map { calories ->
                binding.caloriesEditText.run {
                    if (!hasFocus()) {
                        text = Editable.Factory.getInstance().newEditable(calories.toString())
                    }
                }
            }
            binding.greenieSwitch.isChecked = it.withGreenie
            binding.dryfoodSwitch.isChecked = it.withDryFood
            it.selectedFood.map { foodType ->
                binding.foodDropdown.run {
                    setText(adapter.getItem(foodType.position).toString(), false)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.state.collect {
                setupFoodPicker(it)
                when (it) {
                    is MainState.Error -> throw it.throwable
                    is MainState.Loading -> setupFoodPicker(it)
                    is MainState.NeedInput -> {
                        populateViews(it)
                    }
                    is MainState.Success -> {
                        binding.wetFoodTotal.text = it.mealServing.wetFoodServing.toString()
                        binding.dryFoodTotal.text = it.mealServing.dryFoodServing.toString()
                        populateViews(it)
                    }
                }.exhaustive
                setupUserInteraction(it)
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
