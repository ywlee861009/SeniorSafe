package com.seniorsafe.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_TOKEN     = stringPreferencesKey("access_token")
        private val KEY_USER_TYPE = stringPreferencesKey("user_type")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_ID   = stringPreferencesKey("user_id")
    }

    suspend fun saveAuth(token: String, userType: String, name: String, userId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN]     = token
            prefs[KEY_USER_TYPE] = userType
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_ID]   = userId
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[KEY_TOKEN] }.first()

    suspend fun getUserType(): String? =
        context.dataStore.data.map { it[KEY_USER_TYPE] }.first()

    suspend fun getUserName(): String? =
        context.dataStore.data.map { it[KEY_USER_NAME] }.first()

    suspend fun isLoggedIn(): Boolean = getAccessToken() != null

    suspend fun clear() { context.dataStore.edit { it.clear() } }
}
