package io.github.junaaid96.colorvision.feature

import android.content.Intent
import android.os.Build
import android.provider.Settings as SystemSettings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.data.ThemeMode
import io.github.junaaid96.colorvision.ui.AppViewModel
import io.github.junaaid96.colorvision.ui.ScreenFrame

@Composable
fun SettingsScreen(vm: AppViewModel, onReplayOnboarding: () -> Unit) {
    val s = vm.settings.collectAsStateWithLifecycle().value ?: return
    val context = LocalContext.current
    ScreenFrame(stringResource(R.string.nav_settings)) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
            Text(stringResource(R.string.set_theme), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleSmall)
            SingleChoiceSegmentedButtonRow(Modifier.padding(16.dp)) {
                val modes = ThemeMode.entries
                modes.forEachIndexed { i, m ->
                    SegmentedButton(selected = s.theme == m, onClick = { vm.updateSettings { it.copy(theme = m) } }, shape = SegmentedButtonDefaults.itemShape(i, modes.size)) {
                        Text(stringResource(when (m) { ThemeMode.SYSTEM -> R.string.set_theme_system; ThemeMode.LIGHT -> R.string.set_theme_light; ThemeMode.DARK -> R.string.set_theme_dark }))
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                Toggle(R.string.set_dynamic, R.string.set_dynamic_desc, s.dynamicColor) { v -> vm.updateSettings { it.copy(dynamicColor = v) } }
            Toggle(R.string.set_contrast, R.string.set_contrast_desc, s.highContrast) { v -> vm.updateSettings { it.copy(highContrast = v) } }
            Toggle(R.string.set_motion, R.string.set_motion_desc, s.reduceMotion) { v -> vm.updateSettings { it.copy(reduceMotion = v) } }
            Toggle(R.string.set_speech, R.string.set_speech_desc, s.speech) { v -> vm.updateSettings { it.copy(speech = v) } }
            Toggle(R.string.set_haptics, R.string.set_haptics_desc, s.haptics) { v -> vm.updateSettings { it.copy(haptics = v) } }
            HorizontalDivider()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.set_language)) },
                    supportingContent = { Text(stringResource(R.string.set_language_desc)) },
                    trailingContent = {
                        TextButton(onClick = {
                            context.startActivity(Intent(SystemSettings.ACTION_APP_LOCALE_SETTINGS, "package:${context.packageName}".toUri()))
                        }) { Text(stringResource(R.string.action_open)) }
                    },
                )
            }
            ListItem(
                headlineContent = { Text(stringResource(R.string.set_onboarding)) },
                trailingContent = { TextButton(onClick = onReplayOnboarding) { Text(stringResource(R.string.action_open)) } },
            )
            HorizontalDivider()
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.set_privacy), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.set_privacy_body), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.set_about), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.disclaimer_full), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun Toggle(title: Int, desc: Int, value: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(title)) },
        supportingContent = { Text(stringResource(desc)) },
        trailingContent = { Row { Switch(value, onChange) } },
    )
}
