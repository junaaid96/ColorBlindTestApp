package io.github.junaaid96.colorvision.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.data.HistoryEntry
import io.github.junaaid96.colorvision.data.TestKind
import io.github.junaaid96.colorvision.tests.ArrangementResult
import io.github.junaaid96.colorvision.tests.Classification
import io.github.junaaid96.colorvision.tests.HueArrangement
import io.github.junaaid96.colorvision.tests.Severity
import io.github.junaaid96.colorvision.color.CvdType
import io.github.junaaid96.colorvision.ui.AppViewModel
import io.github.junaaid96.colorvision.ui.ScreenFrame

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArrangementScreen(vm: AppViewModel, onBack: () -> Unit) {
    val caps = remember { HueArrangement.caps().map { Color(it.toArgb()) } }
    var seed by rememberSaveable { mutableStateOf(System.nanoTime()) }
    val pool = remember(seed) { (1..HueArrangement.CAPS).shuffled(kotlin.random.Random(seed)) }
    val placed = remember(seed) { mutableStateListOf<Int>() }
    var result by remember(seed) { mutableStateOf<ArrangementResult?>(null) }
    val haptic = LocalHapticFeedback.current

    ScreenFrame(stringResource(R.string.home_arrange), onBack) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.arrange_instr), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.arrange_sequence), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Cap(caps[0], stringResource(R.string.arrange_pilot), bordered = true) {}
                placed.forEachIndexed { pos, cap ->
                    Cap(caps[cap], stringResource(R.string.arrange_placed, pos + 1)) { if (result == null) placed.remove(cap) }
                }
            }
            Text(stringResource(R.string.arrange_pool), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                pool.filter { it !in placed }.forEachIndexed { i, cap ->
                    Cap(caps[cap], stringResource(R.string.arrange_cap, i + 1)) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        placed.add(cap)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { seed = System.nanoTime() }) { Text(stringResource(R.string.action_reset)) }
                Button(enabled = placed.size == HueArrangement.CAPS && result == null, onClick = {
                    val r = HueArrangement.score(placed.toList())
                    result = r
                    val cls = when {
                        r.passed -> Classification.TYPICAL
                        r.likelyAxis == CvdType.PROTAN -> Classification.PROTAN
                        r.likelyAxis == CvdType.DEUTAN -> Classification.DEUTAN
                        else -> Classification.TRITAN
                    }
                    val sev = when { r.passed -> Severity.NONE; r.cIndex < 2.2 -> Severity.MILD; r.cIndex < 3.0 -> Severity.MODERATE; else -> Severity.STRONG }
                    vm.save(HistoryEntry(System.currentTimeMillis(), TestKind.ARRANGEMENT, cls, sev, (r.cIndex * 100).toInt(), ((r.sIndex / 3).coerceAtMost(1.0) * 100).toInt()))
                }) { Text(stringResource(R.string.action_finish)) }
            }
            result?.let { r ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (r.passed) stringResource(R.string.arrange_pass)
                            else stringResource(R.string.arrange_fail, stringResource(r.likelyAxis!!.label())),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(stringResource(R.string.arrange_stats, r.cIndex, r.sIndex, r.crossings))
                        Text(stringResource(R.string.disclaimer_short), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun Cap(color: Color, description: String, bordered: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .background(color, CircleShape)
            .then(if (bordered) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
    )
}
