package io.github.junaaid96.colorvision.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.data.HistoryEntry
import io.github.junaaid96.colorvision.data.TestKind
import io.github.junaaid96.colorvision.tests.Classification
import io.github.junaaid96.colorvision.tests.OddHue
import io.github.junaaid96.colorvision.tests.Severity
import io.github.junaaid96.colorvision.ui.AppViewModel
import io.github.junaaid96.colorvision.ui.ScreenFrame
import kotlinx.coroutines.delay

/** Daily hue-discrimination practice. Three lives; each correct tile raises the level. */
@Composable
fun PracticeScreen(vm: AppViewModel, onBack: () -> Unit) {
    val best by vm.bestLevel.collectAsStateWithLifecycle()
    val streak by vm.streak.collectAsStateWithLifecycle()
    var level by remember { mutableIntStateOf(1) }
    var lives by remember { mutableIntStateOf(3) }
    var secondsLeft by remember { mutableIntStateOf(60) }
    var over by remember { mutableStateOf(false) }
    val round = remember(level, lives) { OddHue.round(level) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(over) {
        while (!over && secondsLeft > 0) { delay(1000); secondsLeft-- }
        if (!over) over = true
    }
    LaunchedEffect(over) {
        if (over) {
            vm.recordPractice(level)
            vm.save(HistoryEntry(System.currentTimeMillis(), TestKind.PRACTICE, Classification.TYPICAL, Severity.NONE, level, 100))
        }
    }

    ScreenFrame(stringResource(R.string.home_practice), onBack) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.practice_status, level, lives, secondsLeft), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.practice_best, best, streak.best), style = MaterialTheme.typography.bodyMedium)
            if (!over) {
                Text(stringResource(R.string.practice_instr))
                Column(Modifier.widthIn(max = 420.dp).fillMaxWidth().aspectRatio(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (r in 0 until round.size) Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (c in 0 until round.size) {
                            val idx = r * round.size + c
                            val color = Color(if (idx == round.oddIndex) round.odd.toArgb() else round.base.toArgb())
                            val label = stringResource(R.string.practice_tile, r + 1, c + 1)
                            Box(
                                Modifier.weight(1f).fillMaxWidth().aspectRatio(1f)
                                    .background(color, RoundedCornerShape(12.dp))
                                    .semantics { contentDescription = label }
                                    .clickable {
                                        if (idx == round.oddIndex) { haptic.performHapticFeedback(HapticFeedbackType.Confirm); level++ }
                                        else { haptic.performHapticFeedback(HapticFeedbackType.Reject); lives--; if (lives <= 0) over = true }
                                    },
                            )
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.practice_over, level), style = MaterialTheme.typography.headlineMedium)
                Button(onClick = { level = 1; lives = 3; secondsLeft = 60; over = false }) { Text(stringResource(R.string.action_again)) }
            }
        }
    }
}
