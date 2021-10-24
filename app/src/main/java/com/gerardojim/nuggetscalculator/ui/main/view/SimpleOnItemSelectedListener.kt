package com.gerardojim.nuggetscalculator.ui.main.view

import android.view.View
import android.widget.AdapterView

class SimpleOnItemSelectedListener(
    val listener: (Int) -> Unit
) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) { listener.invoke(position) }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // No - Op
    }
}
