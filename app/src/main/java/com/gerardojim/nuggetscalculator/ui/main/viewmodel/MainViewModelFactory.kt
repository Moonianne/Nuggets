package com.gerardojim.nuggetscalculator.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gerardojim.nuggetscalculator.ui.main.AndroidPreferences
import com.gerardojim.nuggetscalculator.ui.main.domain.GetCalculatorViewStateUseCase

class MainViewModelFactory(private val androidPreferences: AndroidPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculateViewModel::class.java)) {
            return CalculateViewModel(
                useCase = GetCalculatorViewStateUseCase(androidPreferences),
                appPrefs = androidPreferences,
            ) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}
