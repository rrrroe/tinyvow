package com.rrrrz.tinyvow.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.managedAppDataStore by preferencesDataStore(name = "managed_app_preferences")

class ManagedAppPreferences(
    private val context: Context,
) {
    private object Keys {
        val selectedPackageName = stringPreferencesKey("selected_package_name")
    }

    val selectedPackageName: Flow<String?> = context.managedAppDataStore.data.map { preferences ->
        preferences[Keys.selectedPackageName]
    }

    suspend fun setSelectedPackageName(packageName: String) {
        context.managedAppDataStore.edit { preferences ->
            preferences[Keys.selectedPackageName] = packageName
        }
    }
}
