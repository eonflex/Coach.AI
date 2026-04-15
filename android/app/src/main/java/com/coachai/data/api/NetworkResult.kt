package com.coachai.data.api

/**
 * Wraps every network call result so ViewModels never see raw exceptions.
 *
 * Usage:
 *   val result = safeApiCall { api.getFoodItems() }
 *   when (result) {
 *       is NetworkResult.Success -> result.data
 *       is NetworkResult.Error   -> showError(result.message)
 *       is NetworkResult.Loading -> showSpinner()
 *   }
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

/**
 * Executes [block] inside a try-catch and wraps the result in [NetworkResult].
 * HTTP error codes from Retrofit are surfaced via [retrofit2.Response.code].
 */
suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(block())
    } catch (e: retrofit2.HttpException) {
        NetworkResult.Error(
            message = e.response()?.errorBody()?.string() ?: e.message(),
            code = e.code()
        )
    } catch (e: java.io.IOException) {
        NetworkResult.Error(message = "Network error: ${e.message ?: "check your connection"}")
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message ?: "Unexpected error")
    }
}
