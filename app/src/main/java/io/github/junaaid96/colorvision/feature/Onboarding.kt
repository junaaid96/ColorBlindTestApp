package io.github.junaaid96.colorvision.feature

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.ui.theme.LocalReduceMotion

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val reduce = LocalReduceMotion.current
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearProgressIndicator(progress = { (page + 1) / 3f }, modifier = Modifier.fillMaxWidth())
        AnimatedContent(
            targetState = page,
            transitionSpec = { if (reduce) fadeIn(snap()) togetherWith fadeOut(snap()) else fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            modifier = Modifier.weight(1f).widthIn(max = 600.dp),
            label = "onboarding",
        ) { p ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (p) {
                    0 -> Page(Icons.Outlined.Palette, stringResource(R.string.onb_welcome_title), stringResource(R.string.onb_welcome_body))
                    1 -> Page(Icons.Outlined.HealthAndSafety, stringResource(R.string.onb_disclaimer_title), stringResource(R.string.disclaimer_full))
                    else -> {
                        Page(Icons.Outlined.Lightbulb, stringResource(R.string.onb_calibrate_title), stringResource(R.string.onb_calibrate_body))
                        Tip(Icons.Outlined.BrightnessHigh, stringResource(R.string.tip_brightness))
                        Tip(Icons.Outlined.DarkMode, stringResource(R.string.tip_night))
                        Tip(Icons.Outlined.Lightbulb, stringResource(R.string.tip_light))
                        Tip(Icons.Outlined.PhoneAndroid, stringResource(R.string.tip_distance))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().widthIn(max = 600.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            if (page > 0) TextButton(onClick = { page-- }) { Text(stringResource(R.string.action_back)) } else Spacer(Modifier)
            Button(onClick = { if (page < 2) page++ else onDone() }) {
                Text(stringResource(if (page < 2) R.string.action_next else R.string.action_get_started))
            }
        }
    }
}

@Composable
private fun Page(icon: ImageVector, title: String, body: String) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
    Text(title, style = MaterialTheme.typography.displaySmall, modifier = Modifier.semantics { heading() })
    Text(body, style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun Tip(icon: ImageVector, text: String) {
    ListItem(leadingContent = { Icon(icon, contentDescription = null) }, headlineContent = { Text(text) })
}
