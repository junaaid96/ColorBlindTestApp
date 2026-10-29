package io.github.junaaid96.colorvision.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore(name = "color_vision")

/** Single source of truth for settings, history and streaks. Nothing leaves the device. */
class Repository(private val context: Context) {
    private object K {
        val onboarded = booleanPreferencesKey("onboarded")
        val theme = stringPreferencesKey("theme")
        val dynamic = booleanPreferencesKey("dynamic")
        val contrast = booleanPreferencesKey("high_contrast")
        val motion = booleanPreferencesKey("reduce_motion")
        val speech = booleanPreferencesKey("speech")
        val haptics = booleanPreferencesKey("haptics")
        val history = stringPreferencesKey("history")
        val streak = intPreferencesKey("streak")
        val best = intPreferencesKey("best")
        val lastDay = longPreferencesKey("last_day")
        val bestLevel = intPreferencesKey("best_level")
    }

    val settings: Flow<Settings> = context.store.data.map { p ->
        Settings(
            onboarded = p[K.onboarded] ?: false,
            theme = p[K.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            dynamicColor = p[K.dynamic] ?: true,
            highContrast = p[K.contrast] ?: false,
            reduceMotion = p[K.motion] ?: false,
            speech = p[K.speech] ?: false,
            haptics = p[K.haptics] ?: true,
        )
    }

    val history: Flow<List<HistoryEntry>> = context.store.data.map { HistoryEntry.decodeAll(it[K.history]) }
    val streak: Flow<Streak> = context.store.data.map { Streak(it[K.streak] ?: 0, it[K.best] ?: 0, it[K.lastDay] ?: -1) }
    val bestLevel: Flow<Int> = context.store.data.map { it[K.bestLevel] ?: 0 }

    suspend fun update(block: (Settings) -> Settings) {
        context.store.edit { p ->
            val cur = Settings(
                p[K.onboarded] ?: false,
                p[K.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
                p[K.dynamic] ?: true, p[K.contrast] ?: false, p[K.motion] ?: false, p[K.speech] ?: false, p[K.haptics] ?: true,
            )
            val n = block(cur)
            p[K.onboarded] = n.onboarded; p[K.theme] = n.theme.name; p[K.dynamic] = n.dynamicColor
            p[K.contrast] = n.highContrast; p[K.motion] = n.reduceMotion; p[K.speech] = n.speech; p[K.haptics] = n.haptics
        }
    }

    suspend fun add(entry: HistoryEntry) {
        context.store.edit { p ->
            val list = (HistoryEntry.decodeAll(p[K.history]) + entry).takeLast(200)
            p[K.history] = HistoryEntry.encodeAll(list)
        }
    }

    suspend fun clearHistory() { context.store.edit { it.remove(K.history) } }

    suspend fun recordPractice(today: Long, level: Int) {
        context.store.edit { p ->
            val s = Streak(p[K.streak] ?: 0, p[K.best] ?: 0, p[K.lastDay] ?: -1).record(today)
            p[K.streak] = s.current; p[K.best] = s.best; p[K.lastDay] = s.lastDay
            p[K.bestLevel] = maxOf(p[K.bestLevel] ?: 0, level)
        }
    }

}
