package com.seniorsafe.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.seniorsafe.core.model.DeviceRegistrationDraft
import com.seniorsafe.core.model.DeviceRole
import com.seniorsafe.core.model.LocalDeviceState
import com.seniorsafe.core.model.PairingStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.deviceDataStore: DataStore<Preferences> by preferencesDataStore(name = "device")

@Singleton
class DeviceDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_LOCAL_DEVICE_ID = stringPreferencesKey("local_device_id")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_PAIRING_STATUS = stringPreferencesKey("pairing_status")
    }

    val deviceState: Flow<LocalDeviceState> =
        context.deviceDataStore.data.map { prefs ->
            LocalDeviceState(
                localDeviceId = prefs[KEY_LOCAL_DEVICE_ID],
                role = DeviceRole.fromValue(prefs[KEY_ROLE]),
                pairingStatus = PairingStatus.fromValue(prefs[KEY_PAIRING_STATUS])
            )
        }

    /**
     * Identifies this app installation until app data is cleared or the app is reinstalled.
     * A reinstall is intentionally treated as a new device for the sessionless MVP.
     */
    suspend fun ensureLocalDeviceId(): String {
        val existing = context.deviceDataStore.data
            .map { prefs -> prefs[KEY_LOCAL_DEVICE_ID] }
            .first()

        if (existing != null) return existing

        val newId = UUID.randomUUID().toString()
        context.deviceDataStore.edit { prefs ->
            if (prefs[KEY_LOCAL_DEVICE_ID] == null) {
                prefs[KEY_LOCAL_DEVICE_ID] = newId
            }
        }

        return context.deviceDataStore.data
            .map { prefs -> prefs[KEY_LOCAL_DEVICE_ID] ?: newId }
            .first()
    }

    suspend fun saveRole(role: DeviceRole) {
        ensureLocalDeviceId()
        context.deviceDataStore.edit { prefs ->
            prefs[KEY_ROLE] = role.value
            prefs[KEY_PAIRING_STATUS] = prefs[KEY_PAIRING_STATUS] ?: PairingStatus.UNPAIRED.value
        }
    }

    suspend fun savePairingStatus(status: PairingStatus) {
        ensureLocalDeviceId()
        context.deviceDataStore.edit { prefs ->
            prefs[KEY_PAIRING_STATUS] = status.value
        }
    }

    suspend fun getDeviceState(): LocalDeviceState = deviceState.first()

    /**
     * Boundary for the future server device registration request.
     * Returns null until the user has selected a role.
     */
    suspend fun getRegistrationDraft(): DeviceRegistrationDraft? {
        val state = getDeviceState()
        val localDeviceId = state.localDeviceId ?: return null
        val role = state.role ?: return null
        return DeviceRegistrationDraft(
            localDeviceId = localDeviceId,
            role = role
        )
    }

    suspend fun clearRoleAndPairing() {
        context.deviceDataStore.edit { prefs ->
            prefs.remove(KEY_ROLE)
            prefs.remove(KEY_PAIRING_STATUS)
        }
    }
}
