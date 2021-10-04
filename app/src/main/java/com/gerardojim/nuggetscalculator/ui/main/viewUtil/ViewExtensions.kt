package com.gerardojim.nuggetscalculator.ui.main.viewUtil

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.CompoundButton
import android.widget.EditText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
fun View.clicks(): Flow<Unit> = callbackFlow {
    val listener = View.OnClickListener {
        trySend(Unit)
    }
    setOnClickListener(listener)
    awaitClose { setOnClickListener(null) }
}

@ExperimentalCoroutinesApi
fun EditText.textChanges(): Flow<Editable> = callbackFlow {
    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            p0?.let { trySend(it) }
        }
    }
    addTextChangedListener(textWatcher)
    awaitClose { removeTextChangedListener(textWatcher) }
}

@ExperimentalCoroutinesApi
fun CompoundButton.checkedChanges(): Flow<Boolean> = callbackFlow {
    trySend(isChecked)
    setOnCheckedChangeListener { button, isChecked ->
        this.trySend(isChecked)
    }
    awaitClose { setOnCheckedChangeListener(null) }
}

@ExperimentalCoroutinesApi
fun AutoCompleteTextView.selectionChanges(): Flow<Int> = callbackFlow {
    setOnItemClickListener { adapterView, view, position, id ->
        this.trySend(position)
    }
    awaitClose { onItemClickListener = null }
}