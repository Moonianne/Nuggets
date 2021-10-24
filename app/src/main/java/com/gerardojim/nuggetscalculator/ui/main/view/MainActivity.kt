package com.gerardojim.nuggetscalculator.ui.main.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gerardojim.nuggetscalculator.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, CalculateFragment.newInstance())
                .commitNow()
        }
    }
}
