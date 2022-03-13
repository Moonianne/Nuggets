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
        private const val CALORIC_TARGET: String = "caloricTarget"
        private const val FOOD_TYPE: String = "foodType"
        private const val WITH_GREENIE: String = "withGreenie"
        private const val WITH_DRY_FOOD: String = "withDryFood"
    }

    override fun getCaloricTarget(): Option<Int> = if (sharedPreferences.contains(CALORIC_TARGET)) {
        sharedPreferences.getInt(CALORIC_TARGET, 0).some()
    } else none()

    override fun setCaloricTarget(caloricTarget: CalculateIntent.DailyCaloricTarget): Boolean =
        sharedPreferences.edit().putInt(CALORIC_TARGET, caloricTarget.value).commit()

    override fun getFoodType(): Option<FoodType> =
        sharedPreferences.getString(FOOD_TYPE, null).toOption().map {
            FoodType.fromKey(it)
        }

    override fun setFoodType(foodType: FoodType): Boolean =
        sharedPreferences.edit().putString(FOOD_TYPE, foodType.key).commit()

    override fun isWithGreenie(): Boolean = sharedPreferences.getBoolean(WITH_GREENIE, false)

    override fun setWithGreenie(withGreenie: CalculateIntent.HasGreenie): Boolean =
        sharedPreferences.edit().putBoolean(WITH_GREENIE, withGreenie.value).commit()

    override fun isCombinedWithDryFood() = sharedPreferences.getBoolean(WITH_DRY_FOOD, false)

    override fun setCombinedWithDryFood(withDryFood: CalculateIntent.HasDryFood): Boolean =
        sharedPreferences.edit().putBoolean(WITH_DRY_FOOD, withDryFood.value).commit()
}
