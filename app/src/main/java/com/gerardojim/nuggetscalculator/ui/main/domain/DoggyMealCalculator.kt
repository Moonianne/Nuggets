package com.gerardojim.nuggetscalculator.ui.main.domain

import kotlin.math.floor

private const val KCAL_PER_GREENIE = 26
private const val FULL_PORTION = 2
private const val HALF_PORTION = 4

class DoggyMealCalculator {

    fun getMealServing(
        wetFood: FoodType,
        dailyCaloricTarget: Int,
        hasDryFood: Boolean,
        hasGreenie: Boolean,
        offset: Double = 0.0,
    ): MealServing {

        fun getFoodServingInGrams(kcalPerServing: Double, foodType: FoodType): Double =
            (kcalPerServing / foodType.kcalPerGram)

        val adjustedCaloricTarget = (dailyCaloricTarget + (dailyCaloricTarget * offset))
        val dailyCaloricMealTarget =
            if (hasGreenie) adjustedCaloricTarget - KCAL_PER_GREENIE else adjustedCaloricTarget
        val kCalTargetPerPortion =
            dailyCaloricMealTarget / (if (hasDryFood) HALF_PORTION else FULL_PORTION)
        val dryFoodServing =
            if (hasDryFood) getFoodServingInGrams(kCalTargetPerPortion, FoodType.PEPITAS) else 0.0

        val wetFoodServing = getFoodServingInGrams(kCalTargetPerPortion, wetFood)

        return MealServing(
            wetFoodServing = floor(wetFoodServing).toInt(),
            dryFoodServing = floor(dryFoodServing).toInt(),
            includeGreenie = hasGreenie
        )
    }
}
