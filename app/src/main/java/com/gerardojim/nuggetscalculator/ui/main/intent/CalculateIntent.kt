package com.gerardojim.nuggetscalculator.ui.main.intent

import com.gerardojim.nuggetscalculator.ui.main.domain.FoodType

sealed interface CalculateIntent {
    @JvmInline
    value class DailyCaloricTarget(val value: Int) : CalculateIntent

    @JvmInline
    value class HasDryFood(val value: Boolean) : CalculateIntent

    @JvmInline
    value class HasGreenie(val value: Boolean) : CalculateIntent

    data class FoodSelected(val selectedFood: FoodType) : CalculateIntent
}
