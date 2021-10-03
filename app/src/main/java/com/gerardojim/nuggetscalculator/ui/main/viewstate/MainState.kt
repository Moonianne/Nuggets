package com.gerardojim.nuggetscalculator.ui.main.viewstate

import com.gerardojim.nuggetscalculator.ui.main.domain.FoodType
import com.gerardojim.nuggetscalculator.ui.main.domain.MealServing

sealed class MainState {

    data class Init(
        val foodTypes: List<FoodType> = FoodType.values().sortedBy { it.position }
    ) : MainState()

    data class Working(
        val selectedFood: FoodType = FoodType.CHICKEN,
        val withGreenie: Boolean = true,
        val withDryFood: Boolean = true,
    ) : MainState()

    data class Results(val mealServing: MealServing) : MainState()

    data class Error(
        val throwable: Throwable,
        val details: String?,
    ) : MainState()
}