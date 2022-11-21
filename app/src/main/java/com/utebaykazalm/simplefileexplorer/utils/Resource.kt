package com.utebaykazalm.simplefileexplorer.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


sealed class Resource<T>(open val data: T?, open val message: String?) {
    class Success<T>(override val data: T) : Resource<T>(data, null)
    class Error<T>(override val message: String) : Resource<T>(null, message)
    //class Loading<T> : Resource<T>(null, null)
}

fun <T> Fragment.collectLatestFlowInStarted(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}