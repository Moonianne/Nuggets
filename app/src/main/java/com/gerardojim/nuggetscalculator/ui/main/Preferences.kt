package com.gerardojim.nuggetscalculator.ui.main

import arrow.core.Option
import com.gerardojim.nuggetscalculator.ui.main.domain.FoodType
import com.gerardojim.nuggetscalculator.ui.main.intent.CalculateIntent

interface Preferences {
    fun getCaloricTarget(): Option<Int>
    fun setCaloricTarget(caloricTarget: CalculateIntent.DailyCaloricTarget)
    fun getFoodType(): Option<FoodType>
    fun setFoodType(foodType: FoodType)
    fun isWithGreenie(): Boolean
    fun setWithGreenie(withGreenie: CalculateIntent.HasGreenie)
    fun isCombinedWithDryFood(): Boolean
    fun setCombinedWithDryFood(withDryFood: CalculateIntent.HasDryFood)
}
