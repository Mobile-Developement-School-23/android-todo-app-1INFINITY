package ru.mirea.ivashechkinav.todo.core

import android.widget.EditText
import androidx.annotation.CheckResult
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

@ExperimentalCoroutinesApi
@CheckResult
fun EditText.textChanges(): Flow<CharSequence?> {
    return callbackFlow {
        val listener = addTextChangedListener { trySend(it) }
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}