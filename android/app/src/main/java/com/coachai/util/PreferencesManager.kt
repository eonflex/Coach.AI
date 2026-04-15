package com.coachai.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SERVER_URL = stringPreferencesKey("server_url")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
        const val DEFAULT_URL = "http://10.0.2.2:5000"
        const val DEFAULT_USERNAME = "coach"
        const val DEFAULT_PASSWORD = "changeme"
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { it[SERVER_URL] ?: DEFAULT_URL }
    val username: Flow<String> = context.dataStore.data.map { it[USERNAME] ?: DEFAULT_USERNAME }
    val password: Flow<String> = context.dataStore.data.map { it[PASSWORD] ?: DEFAULT_PASSWORD }

    suspend fun saveSettings(url: String, username: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[SERVER_URL] = url
            prefs[USERNAME] = username
            prefs[PASSWORD] = password
        }
    }
}
