package com.gerardojim.nuggetscalculator.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gerardojim.nuggetscalculator.ui.main.domain.DoggyMealCalculator

class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculateViewModel::class.java)) {
            return CalculateViewModel(DoggyMealCalculator()) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}