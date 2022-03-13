package com.gerardojim.nuggetscalculator.ui.main.domain

enum class FoodType(
    val     position: Int,
    val key: String,
    val kcalPerGram: Double,
) {
    CHICKEN(0, "chicken", 1.517),
    TURKEY(1, "turkey", 1.728),
    BEEF(2, "beef", 1.552),
    VENISON(3, "venison", 0.882),
    LAMB(4, "lamb", 1.482),
    FISH(5, "fish", 0.917),
    PEPITAS(6, "pepitas", 3.475);

    companion object {
        fun fromPosition(position: Int): FoodType = values().first { position == it.position }

        fun fromKey(foodTypeString: String): FoodType {
            return values().first { it.key == foodTypeString }
        }
    }
}
