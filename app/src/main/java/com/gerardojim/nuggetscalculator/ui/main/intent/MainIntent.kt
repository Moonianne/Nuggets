package com.gerardojim.nuggetscalculator.ui.main.intent

sealed class MainIntent {
    data class Calculate(
        val selectedFoodPosition: Int,
        val dailyCaloricTarget: Int,
        val offset: Double = 0.0,
        val hasDryFood: Boolean,
        val hasGreenie: Boolean,
    ) : MainIntent()
}