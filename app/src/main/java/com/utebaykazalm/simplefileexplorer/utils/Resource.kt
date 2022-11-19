package com.utebaykazalm.simplefileexplorer.utils


sealed class Resource<T>(open val data: T?, open val message: String?) {
    class Success<T>(override val data: T) : Resource<T>(data, null)
    class Error<T>(override val message: String) : Resource<T>(null, message)
}