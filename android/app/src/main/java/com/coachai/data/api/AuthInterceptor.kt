package com.coachai.data.api

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {
    @Volatile private var username: String = "coach"
    @Volatile private var password: String = "changeme"

    fun setCredentials(username: String, password: String) {
        this.username = username
        this.password = password
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val credentials = Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
        val request = chain.request().newBuilder()
            .header("Authorization", "Basic $credentials")
            .build()
        return chain.proceed(request)
    }
}
