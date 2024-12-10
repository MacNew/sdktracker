package org.tracker.trackersdk.data

sealed  class Result<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T?): Result<T>(data)
    class Error<T>(data: T? , message: String = "Error"): Result<T>(data, message)
}