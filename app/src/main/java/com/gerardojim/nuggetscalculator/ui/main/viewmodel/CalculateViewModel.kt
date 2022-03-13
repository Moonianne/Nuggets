package com.gerardojim.nuggetscalculator.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.some
import arrow.core.toOption
import com.gerardojim.nuggetscalculator.ui.main.Preferences
import com.gerardojim.nuggetscalculator.ui.main.domain.DoggyMealCalculator
import com.gerardojim.nuggetscalculator.ui.main.domain.FoodType
import com.gerardojim.nuggetscalculator.ui.main.exhaustive
import com.gerardojim.nuggetscalculator.ui.main.get
import com.gerardojim.nuggetscalculator.ui.main.intent.CalculateIntent
import com.gerardojim.nuggetscalculator.ui.main.viewstate.MainState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class CalculateViewModel(
    private val calculator: DoggyMealCalculator,
    private val appPrefs: Preferences,
) : ViewModel() {

    private val userIntent = Channel<CalculateIntent>(Channel.UNLIMITED)
    private val viewState = MutableStateFlow<MainState>(MainState.Loading)
    val state: StateFlow<MainState> get() = viewState

    init {
        handleIntent()
        loadSavedValues()
    }

    private fun loadSavedValues() {
        val isWithGreenie = appPrefs.isWithGreenie()
        val isWithDryFood = appPrefs.isCombinedWithDryFood()
        val caloricTarget = appPrefs.getCaloricTarget()
        val selectedFood = appPrefs.getFoodType()

        if (caloricTarget.isDefined() && selectedFood.isDefined()) {
            viewModelScope.launch {
                userIntent.send(
                    CalculateIntent.Calculate(
                        hasGreenie = isWithGreenie,
                        hasDryFood = isWithDryFood,
                        dailyCaloricTarget = caloricTarget.get(),
                        selectedFood = selectedFood.get(),
                    )
                )
            }
        } else {
            viewModelScope.launch {
                viewState.value = MainState.NeedInput(
                    withDryFood = isWithDryFood,
                    withGreenie = isWithGreenie,
                    selectedFood = selectedFood,
                    caloricTarget = caloricTarget,
                    onCaloricTargetInput = ::onCaloricTargetSet.some(),
                    onSelectedFoodTypeChanged = ::onSelectedFoodTypeChanged.some(),
                    onWithGreenieSwitched = ::onHasGreenieSwitched.some(),
                    onWithDryFoodSwitched = ::onHasDryFoodSwitched.some(),
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
                        is CalculateIntent.Calculate -> calculateMealServingSize(intent)
                        is CalculateIntent.DailyCaloricTarget -> appPrefs.setCaloricTarget(intent)
                        is CalculateIntent.FoodSelected -> appPrefs.setFoodType(intent.selectedFood)
                        is CalculateIntent.HasDryFood -> appPrefs.setCombinedWithDryFood(intent)
                        is CalculateIntent.HasGreenie -> appPrefs.setWithGreenie(intent)
                    }.exhaustive
                }
            }

            val inputCaloriesSource: Flow<CalculateIntent.DailyCaloricTarget> =
                intentSharedSource.filter { it is CalculateIntent.DailyCaloricTarget }
                    .map { it as CalculateIntent.DailyCaloricTarget }
            val selectedFoodSource: Flow<CalculateIntent.FoodSelected> =
                intentSharedSource.filter { it is CalculateIntent.FoodSelected }
                    .map { it as CalculateIntent.FoodSelected }
            val hasGreenieSource: Flow<CalculateIntent.HasGreenie> =
                intentSharedSource.filter { it is CalculateIntent.HasGreenie }
                    .map { it as CalculateIntent.HasGreenie }
            val hasDryFoodSource: Flow<CalculateIntent.HasDryFood> =
                intentSharedSource.filter { it is CalculateIntent.HasDryFood }
                    .map { it as CalculateIntent.HasDryFood }

            launch {
                combine(
                    inputCaloriesSource,
                    selectedFoodSource,
                    hasGreenieSource,
                    hasDryFoodSource,
                ) { calories, food, hasGreenie, hasDryFood ->
                    CalculateIntent.Calculate(
                        selectedFood = food.selectedFood,
                        dailyCaloricTarget = calories.value,
                        hasDryFood = hasDryFood.value,
                        hasGreenie = hasGreenie.value,
                    )
                }.collect { calculateMealServingSize(it) }
            }
        }
    }

    private fun calculateMealServingSize(mainIntent: CalculateIntent.Calculate) {
        viewModelScope.launch {
            viewState.value = try {
                MainState.Success(
                    mealServing = calculator.getMealServing(
                        wetFood = mainIntent.selectedFood,
                        dailyCaloricTarget = mainIntent.dailyCaloricTarget,
                        hasDryFood = mainIntent.hasDryFood,
                        hasGreenie = mainIntent.hasGreenie,
                    ),
                    withDryFood = mainIntent.hasDryFood,
                    withGreenie = mainIntent.hasGreenie,
                    caloricTarget = mainIntent.dailyCaloricTarget.some(),
                    selectedFood = mainIntent.selectedFood.some(),
                    onCaloricTargetInput = ::onCaloricTargetSet.some(),
                    onSelectedFoodTypeChanged = ::onSelectedFoodTypeChanged.some(),
                    onWithGreenieSwitched = ::onHasGreenieSwitched.some(),
                    onWithDryFoodSwitched = ::onHasDryFoodSwitched.some(),
                )
            } catch (e: Exception) {
                MainState.Error(
                    throwable = e,
                    details = e.localizedMessage.toOption(),
                )
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
