package com.gerardojim.nuggetscalculator.ui.main.viewstate

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import com.gerardojim.nuggetscalculator.ui.main.domain.FoodType
import com.gerardojim.nuggetscalculator.ui.main.domain.MealServing

sealed class MainState {
    val foodTypes: List<FoodType> = FoodType.values().sortedBy { it.position }
    abstract val selectedFood: Option<FoodType>
    abstract val withGreenie: Boolean
    abstract val withDryFood: Boolean
    open val caloricTarget: Option<Int> = none()
    open val onCaloricTargetInput: Option<suspend (Int) -> Unit> = none()
    open val onSelectedFoodTypeChanged: Option<suspend (FoodType) -> Unit> = none()
    open val onWithGreenieSwitched: Option<suspend (Boolean) -> Unit> = none()
    open val onWithDryFoodSwitched: Option<suspend (Boolean) -> Unit> = none()

    object Loading : MainState() {
        override val selectedFood: Option<FoodType>
            get() = none()
        override val withGreenie: Boolean
            get() = false
        override val withDryFood: Boolean
            get() = false
    }

    data class NeedInput(
        override val selectedFood: Option<FoodType> = FoodType.CHICKEN.some(),
        override val withGreenie: Boolean,
        override val withDryFood: Boolean,
        override val caloricTarget: Option<Int>,
        override val onCaloricTargetInput: Option<suspend (Int) -> Unit>,
        override val onSelectedFoodTypeChanged: Option<suspend (FoodType) -> Unit>,
        override val onWithGreenieSwitched: Option<suspend (Boolean) -> Unit>,
        override val onWithDryFoodSwitched: Option<suspend (Boolean) -> Unit>,
    ) : MainState()

    data class Success(
        val mealServing: MealServing,
        override val selectedFood: Option<FoodType>,
        override val withGreenie: Boolean,
        override val withDryFood: Boolean,
        override val caloricTarget: Option<Int>,
        override val onCaloricTargetInput: Option<suspend (Int) -> Unit>,
        override val onSelectedFoodTypeChanged: Option<suspend (FoodType) -> Unit>,
        override val onWithGreenieSwitched: Option<suspend (Boolean) -> Unit>,
        override val onWithDryFoodSwitched: Option<suspend (Boolean) -> Unit>,
    ) : MainState()

    data class Error(
        val throwable: Throwable,
        val details: Option<String>,
    ) : MainState() {
        override val selectedFood: Option<FoodType>
            get() = none()
        override val withGreenie: Boolean
            get() = false
        override val withDryFood: Boolean
            get() = false
    }
}
