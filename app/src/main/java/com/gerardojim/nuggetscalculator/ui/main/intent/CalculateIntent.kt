package com.gerardojim.nuggetscalculator.ui.main.intent

import com.gerardojim.nuggetscalculator.ui.main.domain.FoodType

sealed interface CalculateIntent {

    data class Calculate(
        val selectedFood: FoodType,
        val dailyCaloricTarget: Int,
        val offset: Double = 0.0,
        val hasDryFood: Boolean,
        val hasGreenie: Boolean,
    ) : CalculateIntent

    @JvmInline
    value class DailyCaloricTarget(val value: Int) : CalculateIntent

    @JvmInline
    value class HasDryFood(val value: Boolean) : CalculateIntent

    @JvmInline
    value class HasGreenie(val value: Boolean) : CalculateIntent

    data class FoodSelected(val selectedFood: FoodType) : CalculateIntent
}
