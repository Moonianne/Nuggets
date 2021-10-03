package com.gerardojim.nuggetscalculator.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gerardojim.nuggetscalculator.ui.main.domain.DoggyMealCalculator
import com.gerardojim.nuggetscalculator.ui.main.domain.FoodType
import com.gerardojim.nuggetscalculator.ui.main.intent.MainIntent
import com.gerardojim.nuggetscalculator.ui.main.viewstate.MainState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class MainViewModel(private val calculator: DoggyMealCalculator) : ViewModel() {
    val userIntent = Channel<MainIntent>(Channel.UNLIMITED)
    private val viewState = MutableStateFlow<MainState>(MainState.Init())
    val state: StateFlow<MainState> get() = viewState

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            userIntent.consumeAsFlow().collect { intent ->
                when (intent) {
                    is MainIntent.Calculate -> calculateMealServingSize(intent)
                    is MainIntent.SelectFood -> viewModelScope.launch {
                        viewState.value = MainState.Working(
                            FoodType.fromPosition(intent.position)
                        )
                    }
                }
            }
        }
    }

    private fun calculateMealServingSize(mainIntent: MainIntent.Calculate) {
        viewModelScope.launch {
            viewState.value = MainState.Working()
            viewState.value = try {
                MainState.Results(
                    calculator.getMealServing(
                        wetFood = FoodType.values()[mainIntent.selectedFoodPosition],
                        dailyCaloricTarget = mainIntent.dailyCaloricTarget,
                        hasDryFood = mainIntent.hasDryFood,
                        hasGreenie = mainIntent.hasGreenie,
                    )
                )
            } catch (e: Exception) {
                MainState.Error(
                    throwable = e,
                    details = e.localizedMessage,
                )
            }
        }
    }
}