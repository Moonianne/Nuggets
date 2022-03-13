package com.gerardojim.nuggetscalculator.ui.main.view

import android.view.View
import android.widget.AdapterView

class SimpleOnItemClickListener(
    private val listener: (Int) -> Unit
) : AdapterView.OnItemClickListener {

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        listener(position)
    }
}
