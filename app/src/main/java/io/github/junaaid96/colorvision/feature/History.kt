package io.github.junaaid96.colorvision.feature

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.data.TestKind
import io.github.junaaid96.colorvision.ui.AppViewModel
import io.github.junaaid96.colorvision.ui.ScreenFrame
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(vm: AppViewModel) {
    val history by vm.history.collectAsStateWithLifecycle()
    var kind by rememberSaveable { mutableStateOf(TestKind.QUICK) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    val entries = history.filter { it.kind == kind || (kind == TestKind.QUICK && it.kind == TestKind.FULL) }
    val fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    ScreenFrame(stringResource(R.string.nav_history)) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(TestKind.QUICK, TestKind.ARRANGEMENT, TestKind.CONTRAST, TestKind.PRACTICE).forEach { k ->
                        FilterChip(selected = kind == k, onClick = { kind = k }, label = { Text(stringResource(k.label())) })
                    }
                }
            }
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.history_trend), style = MaterialTheme.typography.titleMedium)
                        val pts = entries.sortedBy { it.timestamp }.takeLast(30).map { it.score.toFloat() }
                        val line = MaterialTheme.colorScheme.primary
                        val grid = MaterialTheme.colorScheme.outlineVariant
                        val desc = stringResource(R.string.history_chart_desc, pts.size, pts.lastOrNull()?.toInt() ?: 0)
                        Canvas(Modifier.fillMaxWidth().height(160.dp).padding(top = 8.dp).semantics { contentDescription = desc }) {
                            for (i in 0..4) { val y = size.height * i / 4; drawLine(grid, Offset(0f, y), Offset(size.width, y), 1f) }
                            if (pts.size >= 2) {
                                val max = pts.max().coerceAtLeast(1f); val min = minOf(0f, pts.min())
                                val p = pts.mapIndexed { i, v -> Offset(size.width * i / (pts.size - 1), size.height * (1 - (v - min) / (max - min))) }
                                p.zipWithNext { a, b -> drawLine(line, a, b, 6f, StrokeCap.Round) }
                                p.forEach { drawCircle(line, 8f, it) }
                            } else if (pts.size == 1) drawCircle(line, 10f, center)
                        }
                    }
                }
            }
            if (entries.isEmpty()) item { Text(stringResource(R.string.history_empty), Modifier.padding(16.dp)) }
            items(entries, key = { it.timestamp }) { e ->
                ListItem(
                    headlineContent = { Text(stringResource(e.classification.label())) },
                    supportingContent = { Text(stringResource(R.string.history_row, stringResource(e.kind.label()), stringResource(e.severity.label()), e.score)) },
                    overlineContent = { Text(fmt.format(Date(e.timestamp))) },
                )
            }
            if (history.isNotEmpty()) item { TextButton(onClick = { confirm = true }) { Text(stringResource(R.string.history_clear)) } }
        }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text(stringResource(R.string.history_clear)) },
        text = { Text(stringResource(R.string.history_clear_body)) },
        confirmButton = { TextButton(onClick = { vm.clearHistory(); confirm = false }) { Text(stringResource(R.string.action_delete)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.action_cancel)) } },
    )
}
