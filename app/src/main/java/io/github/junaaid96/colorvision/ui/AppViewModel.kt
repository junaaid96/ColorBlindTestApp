package io.github.junaaid96.colorvision.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.junaaid96.colorvision.ColorVisionApp
import io.github.junaaid96.colorvision.data.HistoryEntry
import io.github.junaaid96.colorvision.data.Repository
import io.github.junaaid96.colorvision.data.Settings
import io.github.junaaid96.colorvision.data.Streak
import io.github.junaaid96.colorvision.data.TestKind
import io.github.junaaid96.colorvision.tests.ScreeningResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Activity-scoped state shared by every screen. */
class AppViewModel(private val repo: Repository) : ViewModel() {
    /** null until DataStore has loaded, so the first frame never flashes onboarding. */
    val settings: StateFlow<Settings?> = repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val history = repo.history.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val streak = repo.streak.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Streak())
    val bestLevel = repo.bestLevel.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _lastResult = MutableStateFlow<Pair<TestKind, ScreeningResult>?>(null)
    val lastResult = _lastResult.asStateFlow()

    fun today() = LocalDate.now().toEpochDay()

    fun updateSettings(block: (Settings) -> Settings) = viewModelScope.launch { repo.update(block) }

    fun saveScreening(kind: TestKind, result: ScreeningResult) {
        _lastResult.value = kind to result
        viewModelScope.launch {
            repo.add(HistoryEntry(System.currentTimeMillis(), kind, result.classification, result.severity, result.score, (result.confidence * 100).toInt()))
        }
    }

    fun save(entry: HistoryEntry) = viewModelScope.launch { repo.add(entry) }
    fun clearHistory() = viewModelScope.launch { repo.clearHistory() }
    fun recordPractice(level: Int) = viewModelScope.launch { repo.recordPractice(today(), level) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ColorVisionApp
                AppViewModel(app.repository)
            }
        }
    }
}
