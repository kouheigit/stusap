package com.example.vocabapp.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "learning_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val autoAudioKey = booleanPreferencesKey("auto_audio_enabled")

    val autoAudioEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { preferences -> preferences[autoAudioKey] ?: false }

    suspend fun setAutoAudioEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences -> preferences[autoAudioKey] = enabled }
    }
}
