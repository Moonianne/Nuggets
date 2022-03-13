package com.gerardojim.nuggetscalculator.ui.main

import android.content.SharedPreferences
import arrow.core.Option
import arrow.core.none
import arrow.core.some
import arrow.core.toOption
import com.gerardojim.nuggetscalculator.ui.main.domain.FoodType
import com.gerardojim.nuggetscalculator.ui.main.intent.CalculateIntent

class AndroidPreferences(private val sharedPreferences: SharedPreferences) : Preferences {

    private companion object Keys {
        const val CALORIC_TARGET: String = "caloricTarget"
        const val FOOD_TYPE: String = "foodType"
        const val WITH_GREENIE: String = "withGreenie"
        const val WITH_DRY_FOOD: String = "withDryFood"
    }

    override fun getCaloricTarget(): Option<Int> = if (sharedPreferences.contains(CALORIC_TARGET)) {
        sharedPreferences.getInt(CALORIC_TARGET, 0).some()
    } else none()

    override fun setCaloricTarget(caloricTarget: CalculateIntent.DailyCaloricTarget) =
        sharedPreferences.edit().putInt(CALORIC_TARGET, caloricTarget.value).apply()

    override fun getFoodType(): Option<FoodType> =
        sharedPreferences.getString(FOOD_TYPE, null).toOption().map {
            FoodType.fromKey(it)
        }

    override fun setFoodType(foodType: FoodType) =
        sharedPreferences.edit().putString(FOOD_TYPE, foodType.key).apply()

    override fun isWithGreenie(): Boolean = sharedPreferences.getBoolean(WITH_GREENIE, false)

    override fun setWithGreenie(withGreenie: CalculateIntent.HasGreenie) =
        sharedPreferences.edit().putBoolean(WITH_GREENIE, withGreenie.value).apply()

    override fun isCombinedWithDryFood() = sharedPreferences.getBoolean(WITH_DRY_FOOD, false)

    override fun setCombinedWithDryFood(withDryFood: CalculateIntent.HasDryFood) =
        sharedPreferences.edit().putBoolean(WITH_DRY_FOOD, withDryFood.value).apply()
}
