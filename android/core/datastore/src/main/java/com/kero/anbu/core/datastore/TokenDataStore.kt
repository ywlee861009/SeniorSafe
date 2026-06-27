package com.kero.anbu.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // device JWT는 암호문으로만 저장한다.
        private val KEY_TOKEN_ENC = stringPreferencesKey("device_access_token_enc")
        // 구버전 평문 토큰 — 최초 조회 시 암호화 후 제거(마이그레이션).
        private val KEY_TOKEN_LEGACY = stringPreferencesKey("device_access_token")
        private val KEY_DEVICE_ID = stringPreferencesKey("device_id")
        private val KEY_USER_TYPE = stringPreferencesKey("user_type")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
    }

    // 토큰 인메모리 캐시 — 매 네트워크 요청마다 디스크 읽기/복호화를 반복하지 않는다.
    @Volatile private var cachedToken: String? = null
    @Volatile private var tokenLoaded = false

    suspend fun saveDeviceAuth(token: String, deviceId: String, role: String, displayName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN_ENC] = TokenCipher.encrypt(token)
            prefs.remove(KEY_TOKEN_LEGACY)
            prefs[KEY_DEVICE_ID] = deviceId
            prefs[KEY_USER_TYPE] = role
            prefs[KEY_USER_NAME] = displayName
        }
        cachedToken = token
        tokenLoaded = true
    }

    /** 코루틴 컨텍스트에서의 토큰 조회(캐시 우선). */
    suspend fun getDeviceAccessToken(): String? {
        if (tokenLoaded) return cachedToken
        val token = loadTokenFromDisk()
        cachedToken = token
        tokenLoaded = true
        return token
    }

    /**
     * 논블로킹 토큰 조회 — OkHttp 인터셉터용.
     * 캐시가 비어있는 최초 1회만 동기 로드하고, 이후에는 메모리에서 즉시 반환한다.
     */
    fun peekDeviceAccessToken(): String? {
        if (tokenLoaded) return cachedToken
        return runBlocking { getDeviceAccessToken() }
    }

    suspend fun getDeviceId(): String? =
        context.dataStore.data.first()[KEY_DEVICE_ID]

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
        cachedToken = null
        tokenLoaded = true
    }

    private suspend fun loadTokenFromDisk(): String? {
        val prefs = context.dataStore.data.first()
        prefs[KEY_TOKEN_ENC]?.let { return TokenCipher.decrypt(it) }
        // 구버전 평문 토큰을 암호화하고 평문은 삭제한다.
        val legacy = prefs[KEY_TOKEN_LEGACY] ?: return null
        context.dataStore.edit { mutablePrefs ->
            mutablePrefs[KEY_TOKEN_ENC] = TokenCipher.encrypt(legacy)
            mutablePrefs.remove(KEY_TOKEN_LEGACY)
        }
        return legacy
    }
}
