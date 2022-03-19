package com.gerardojim.nuggetscalculator.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.some
import arrow.core.toOption
import com.gerardojim.nuggetscalculator.ui.main.Preferences
import com.gerardojim.nuggetscalculator.ui.main.domain.FoodType
import com.gerardojim.nuggetscalculator.ui.main.domain.GetCalculatorViewStateUseCase
import com.gerardojim.nuggetscalculator.ui.main.intent.CalculateIntent
import com.gerardojim.nuggetscalculator.ui.main.viewstate.MainState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalculateViewModel(
    private val useCase: GetCalculatorViewStateUseCase,
    private val appPrefs: Preferences,
) : ViewModel() {

    private val userIntent = Channel<CalculateIntent>(Channel.UNLIMITED)
    private val viewState = MutableStateFlow<MainState>(MainState.Loading)
    val state: StateFlow<MainState> get() = viewState

    init {
        init()
        handleIntent()
    }

    private fun init() {
        viewModelScope.launch {
            try {
                useCase(
                    onCaloricTargetInput = ::onCaloricTargetSet.some(),
                    onSelectedFoodTypeChanged = ::onSelectedFoodTypeChanged.some(),
                    onWithGreenieSwitched = ::onHasGreenieSwitched.some(),
                    onWithDryFoodSwitched = ::onHasDryFoodSwitched.some(),
                ).collect {
                    viewState.value = it
                }
            } catch (e: Exception) {
                viewState.value = MainState.Error(
                    throwable = e,
                    details = e.localizedMessage.toOption(),
                )
            }
        }
    }

    private fun handleIntent() {
        val intentSharedSource: SharedFlow<CalculateIntent> =
            userIntent.consumeAsFlow().shareIn(viewModelScope, SharingStarted.Lazily)
        viewModelScope.launch {
            launch {
                intentSharedSource.collect { intent ->
                    when (intent) {
                        is CalculateIntent.DailyCaloricTarget -> {
                            appPrefs.setCaloricTarget(intent)
                            useCase.inputCalorieTarget(intent.value.some())
                        }
                        is CalculateIntent.FoodSelected -> {
                            appPrefs.setFoodType(intent.selectedFood)
                            useCase.inputSelectedFood(intent.selectedFood)
                        }
                        is CalculateIntent.HasDryFood -> {
                            appPrefs.setCombinedWithDryFood(intent)
                            useCase.inputHasDryFood(intent.value)
                        }
                        is CalculateIntent.HasGreenie -> {
                            appPrefs.setWithGreenie(intent)
                            useCase.inputHasGreenie(intent.value)
                        }
                    }
                }
            }
        }
    }

    private suspend fun onCaloricTargetSet(target: Int) =
        userIntent.send(CalculateIntent.DailyCaloricTarget(target))

    private suspend fun onSelectedFoodTypeChanged(foodType: FoodType) =
        userIntent.send(CalculateIntent.FoodSelected(foodType))

    private suspend fun onHasGreenieSwitched(hasGreenie: Boolean) =
        userIntent.send(CalculateIntent.HasGreenie(hasGreenie))

    private suspend fun onHasDryFoodSwitched(hasDryFood: Boolean) =
        userIntent.send(CalculateIntent.HasDryFood(hasDryFood))
}
