package io.github.junaaid96.colorvision.feature

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.tests.Plate
import io.github.junaaid96.colorvision.tests.PlateGenerator
import io.github.junaaid96.colorvision.tests.PlateScoring
import io.github.junaaid96.colorvision.tests.ScreeningResult
import io.github.junaaid96.colorvision.ui.PlateView
import io.github.junaaid96.colorvision.ui.ScreenFrame
import io.github.junaaid96.colorvision.ui.theme.LocalReduceMotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlateTestState(
    val plates: List<Plate> = emptyList(),
    val index: Int = 0,
    val input: String = "",
    val answers: List<String> = emptyList(),
    val result: ScreeningResult? = null,
)

class PlateTestViewModel : ViewModel() {
    private val _state = MutableStateFlow(PlateTestState())
    val state = _state.asStateFlow()

    fun start(full: Boolean) {
        if (_state.value.plates.isNotEmpty()) return
        viewModelScope.launch {
            // A fresh seed per session stops people memorising answers.
            val plates = withContext(Dispatchers.Default) { PlateGenerator.plan(full, seed = (System.currentTimeMillis() % 100_000).toInt()) }
            _state.update { it.copy(plates = plates) }
        }
    }

    fun type(d: Char) = _state.update { if (it.input.length < 2) it.copy(input = it.input + d) else it }
    fun delete() = _state.update { it.copy(input = it.input.dropLast(1)) }

    fun submit(nothing: Boolean = false) = _state.update { s ->
        val answers = s.answers + if (nothing) "" else s.input
        if (answers.size == s.plates.size) s.copy(answers = answers, input = "", result = PlateScoring.score(s.plates, answers))
        else s.copy(answers = answers, input = "", index = s.index + 1)
    }
}

@Composable
fun PlateTestScreen(full: Boolean, onBack: () -> Unit, onFinished: (ScreeningResult) -> Unit, vm: PlateTestViewModel = viewModel()) {
    vm.start(full)
    val s by vm.state.collectAsStateWithLifecycle()
    s.result?.let { r -> androidx.compose.runtime.LaunchedEffect(r) { onFinished(r) } }
    val haptic = LocalHapticFeedback.current
    val reduce = LocalReduceMotion.current
    ScreenFrame(stringResource(if (full) R.string.home_full else R.string.home_quick), onBack) { pad ->
        if (s.plates.isEmpty()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
            return@ScreenFrame
        }
        BoxWithConstraints(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            val wide = maxWidth > 600.dp
            val plateArea: @Composable (Modifier) -> Unit = { m ->
                Column(m, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(progress = { (s.index + 1f) / s.plates.size }, modifier = Modifier.fillMaxWidth())
                    Text(stringResource(R.string.plate_progress, s.index + 1, s.plates.size), style = MaterialTheme.typography.labelLarge)
                    AnimatedContent(
                        targetState = s.plates[s.index],
                        transitionSpec = { if (reduce) fadeIn(snap()) togetherWith fadeOut(snap()) else (fadeIn(tween(250)) + scaleIn(initialScale = 0.92f)) togetherWith fadeOut(tween(150)) },
                        label = "plate",
                    ) { p -> PlateView(p, Modifier.widthIn(max = 420.dp).fillMaxWidth()) }
                }
            }
            val keypad: @Composable (Modifier) -> Unit = { m ->
                Column(m, verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.plate_prompt), style = MaterialTheme.typography.titleLarge)
                    Text(
                        s.input.ifEmpty { "–" },
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    listOf("123", "456", "789").forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { d ->
                                FilledTonalButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.type(d) }, modifier = Modifier.weight(1f).height(56.dp)) {
                                    Text(d.toString(), style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.submit(nothing = true) }, modifier = Modifier.weight(1f).height(56.dp)) { Text(stringResource(R.string.plate_nothing)) }
                        FilledTonalButton(onClick = { vm.type('0') }, modifier = Modifier.weight(1f).height(56.dp)) { Text("0", style = MaterialTheme.typography.titleLarge) }
                        OutlinedButton(onClick = { vm.delete() }, modifier = Modifier.weight(1f).height(56.dp)) {
                            Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                    Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.Confirm); vm.submit() }, enabled = s.input.isNotEmpty(), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Text(stringResource(R.string.action_next))
                    }
                }
            }
            if (wide) Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) { plateArea(Modifier.weight(1f)); keypad(Modifier.weight(1f)) }
            else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { plateArea(Modifier.weight(1f)); keypad(Modifier) }
        }
    }
}
