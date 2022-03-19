package com.gerardojim.nuggetscalculator.ui.main.domain

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import com.gerardojim.nuggetscalculator.ui.main.Preferences
import com.gerardojim.nuggetscalculator.ui.main.viewstate.MainState
import kotlin.math.floor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

private const val KCAL_PER_GREENIE = 26
private const val FULL_PORTION = 2
private const val HALF_PORTION = 4

class GetCalculatorViewStateUseCase(
    private val appPrefs: Preferences,
) {

    private val inputCalorieTarget = MutableStateFlow<Option<Int>>(none())
    private val inputSelectedFood = MutableStateFlow<Option<FoodType>>(none())
    private val inputHasGreenie = MutableStateFlow(false)
    private val inputHasDryFood = MutableStateFlow(false)

    operator fun invoke(
        onCaloricTargetInput: Option<suspend (Int) -> Unit>,
        onSelectedFoodTypeChanged: Option<suspend (FoodType) -> Unit>,
        onWithGreenieSwitched: Option<suspend (Boolean) -> Unit>,
        onWithDryFoodSwitched: Option<suspend (Boolean) -> Unit>,
    ): Flow<MainState> {
        inputCalorieTarget.value = appPrefs.getCaloricTarget()
        inputSelectedFood.value = appPrefs.getFoodType()
        inputHasGreenie.value = appPrefs.isWithGreenie()
        inputHasDryFood.value = appPrefs.isCombinedWithDryFood()

        return combine(
            inputCalorieTarget,
            inputSelectedFood,
            inputHasGreenie,
            inputHasDryFood,
        ) { maybeCalories, maybeFood, hasGreenie, hasDryFood ->
            val serving = maybeCalories.flatMap { calories ->
                maybeFood.map { foodType ->
                    calculateMealServing(
                        dailyCaloricTarget = calories,
                        wetFood = foodType,
                        hasGreenie = hasGreenie,
                        hasDryFood = hasDryFood,
                    )
                }
            }

            serving.fold(
                ifEmpty = {
                    MainState.NeedInput(
                        caloricTarget = maybeCalories,
                        selectedFood = maybeFood,
                        withGreenie = hasGreenie,
                        withDryFood = hasDryFood,
                        onCaloricTargetInput = onCaloricTargetInput,
                        onSelectedFoodTypeChanged = onSelectedFoodTypeChanged,
                        onWithGreenieSwitched = onWithGreenieSwitched,
                        onWithDryFoodSwitched = onWithDryFoodSwitched,
                    )
                },
                ifSome = {
                    MainState.Success(
                        caloricTarget = maybeCalories,
                        selectedFood = maybeFood,
                        withGreenie = hasGreenie,
                        withDryFood = hasDryFood,
                        onCaloricTargetInput = onCaloricTargetInput,
                        onSelectedFoodTypeChanged = onSelectedFoodTypeChanged,
                        onWithGreenieSwitched = onWithGreenieSwitched,
                        onWithDryFoodSwitched = onWithDryFoodSwitched,
                        mealServing = it,
                    )
                }
            )
        }
    }

    fun inputCalorieTarget(value: Option<Int>) {
        inputCalorieTarget.value = value
    }

    fun inputSelectedFood(value: FoodType) {
        inputSelectedFood.value = value.some()
    }

    fun inputHasGreenie(value: Boolean) {
        inputHasGreenie.value = value
    }

    fun inputHasDryFood(value: Boolean) {
        inputHasDryFood.value = value
    }

    private fun calculateMealServing(
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
