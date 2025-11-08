package com.androphoshop.core.utils

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

val <T> Result<T>.isSuccess: Boolean get() = this is Result.Success
val <T> Result<T>.isError: Boolean get() = this is Result.Error

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Error -> null
}

fun <T> Result<T>.exceptionOrNull(): Exception? = when (this) {
    is Result.Success -> null
    is Result.Error -> exception
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Exception) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
    }
}

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return when (this) {
        is Result.Success -> transform(data)
        is Result.Error -> this
    }
}