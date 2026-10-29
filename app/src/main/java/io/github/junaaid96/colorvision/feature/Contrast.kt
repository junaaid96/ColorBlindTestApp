package io.github.junaaid96.colorvision.feature

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.data.HistoryEntry
import io.github.junaaid96.colorvision.data.TestKind
import io.github.junaaid96.colorvision.tests.Classification
import io.github.junaaid96.colorvision.tests.ContrastStaircase
import io.github.junaaid96.colorvision.tests.Severity
import io.github.junaaid96.colorvision.ui.AppViewModel
import io.github.junaaid96.colorvision.ui.ScreenFrame
import kotlin.random.Random

@Composable
fun ContrastScreen(vm: AppViewModel, onBack: () -> Unit) {
    var run by remember { mutableIntStateOf(0) }
    val stair = remember(run) { ContrastStaircase() }
    var direction by remember(run) { mutableIntStateOf(Random.nextInt(4)) }
    var tick by remember(run) { mutableIntStateOf(0) } // forces recomposition after each answer
    var saved by remember(run) { mutableStateOf(false) }

    ScreenFrame(stringResource(R.string.home_contrast), onBack) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            @Suppress("UNUSED_EXPRESSION") tick
            if (!stair.finished) {
                Text(stringResource(R.string.contrast_instr), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.contrast_trial, stair.trials + 1), style = MaterialTheme.typography.labelLarge)
                val bg = 0.5f
                val fg = bg * (1 - stair.contrast.toFloat())
                Canvas(Modifier.widthIn(max = 360.dp).fillMaxWidth().aspectRatio(1f)) {
                    drawRect(Color(bg, bg, bg))
                    rotate(direction * 90f) {
                        val c = center; val s = size.minDimension * 0.28f; val w = size.minDimension * 0.09f
                        val ink = Color(fg, fg, fg)
                        drawLine(ink, Offset(c.x, c.y + s), Offset(c.x, c.y - s), w, StrokeCap.Round)
                        drawLine(ink, Offset(c.x, c.y - s), Offset(c.x - s * 0.7f, c.y - s * 0.3f), w, StrokeCap.Round)
                        drawLine(ink, Offset(c.x, c.y - s), Offset(c.x + s * 0.7f, c.y - s * 0.3f), w, StrokeCap.Round)
                    }
                }
                val answer: (Int) -> Unit = { d ->
                    stair.answer(d == direction); direction = Random.nextInt(4); tick++
                }
                FilledTonalIconButton(onClick = { answer(0) }) { Icon(Icons.Outlined.ArrowUpward, stringResource(R.string.dir_up)) }
                Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                    FilledTonalIconButton(onClick = { answer(3) }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.dir_left)) }
                    FilledTonalIconButton(onClick = { answer(1) }) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, stringResource(R.string.dir_right)) }
                }
                FilledTonalIconButton(onClick = { answer(2) }) { Icon(Icons.Outlined.ArrowDownward, stringResource(R.string.dir_down)) }
            } else {
                val cs = stair.logSensitivity()
                if (!saved) {
                    saved = true
                    val sev = when { cs >= 1.5 -> Severity.NONE; cs >= 1.2 -> Severity.MILD; cs >= 0.9 -> Severity.MODERATE; else -> Severity.STRONG }
                    vm.save(HistoryEntry(System.currentTimeMillis(), TestKind.CONTRAST, if (sev == Severity.NONE) Classification.TYPICAL else Classification.INCONCLUSIVE, sev, (cs * 100).toInt(), 70))
                }
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.contrast_result, cs), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(if (cs >= 1.5) R.string.contrast_typical else R.string.contrast_low))
                        Text(stringResource(R.string.disclaimer_short), style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(onClick = { run++ }) { Text(stringResource(R.string.action_retake)) }
            }
        }
    }
}
