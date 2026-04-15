package com.coachai.data.api

import com.coachai.util.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that rewrites the host/port of every request with the
 * server URL stored in [PreferencesManager].
 *
 * This allows the user to change the server address in Settings without
 * restarting the app or recreating the Retrofit/OkHttp singleton.
 *
 * The Retrofit base URL is set to a throwaway placeholder (http://localhost/)
 * and this interceptor replaces it at runtime.
 */
@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Read current server URL from DataStore (blocking read is acceptable here
        // because interceptors run on OkHttp's background thread pool).
        val serverUrl = runBlocking { preferencesManager.serverUrl.first() }
            .trimEnd('/')

        val newBaseUrl = try {
            "$serverUrl/".toHttpUrl()
        } catch (e: IllegalArgumentException) {
            // Malformed URL in settings — fall back to original request
            return chain.proceed(originalRequest)
        }

        val newUrl = originalRequest.url.newBuilder()
            .scheme(newBaseUrl.scheme)
            .host(newBaseUrl.host)
            .port(newBaseUrl.port)
            .build()

        val newRequest = originalRequest.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}
