package io.github.junaaid96.colorvision.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.tests.PlateGenerator
import io.github.junaaid96.colorvision.tests.PlateKind
import io.github.junaaid96.colorvision.ui.AppViewModel
import io.github.junaaid96.colorvision.ui.PlateView
import io.github.junaaid96.colorvision.ui.Routes
import io.github.junaaid96.colorvision.ui.ScreenFrame
import io.github.junaaid96.colorvision.ui.theme.Safe

private data class Action(val route: String, val title: Int, val body: Int, val icon: ImageVector, val accent: Color)

@Composable
fun HomeScreen(vm: AppViewModel, onOpen: (String) -> Unit) {
    val streak by vm.streak.collectAsStateWithLifecycle()
    val days = streak.active(vm.today())
    val hero = remember { PlateGenerator.generate(0, PlateKind.CONTROL, "7", 0, seed = 42) }
    val actions = listOf(
        Action(Routes.plates(false), R.string.home_quick, R.string.home_quick_desc, Icons.Outlined.Bolt, Safe.blue),
        Action(Routes.plates(true), R.string.home_full, R.string.home_full_desc, Icons.Outlined.Science, Safe.vermillion),
        Action(Routes.ARRANGE, R.string.home_arrange, R.string.home_arrange_desc, Icons.Outlined.Reorder, Safe.green),
        Action(Routes.CONTRAST, R.string.home_contrast, R.string.home_contrast_desc, Icons.Outlined.Contrast, Safe.purple),
        Action(Routes.PRACTICE, R.string.home_practice, R.string.home_practice_desc, Icons.Outlined.GridView, Safe.orange),
    )
    ScreenFrame(stringResource(R.string.app_name)) { pad ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(280.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = pad.calculateTopPadding(), bottom = 24.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.home_hero_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                            Text(stringResource(R.string.home_hero_body), style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null, tint = Safe.orange)
                                Text(stringResource(R.string.streak_days, days), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        PlateView(hero, Modifier.size(112.dp))
                    }
                }
            }
            items(actions) { a ->
                OutlinedCard(onClick = { onOpen(a.route) }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(a.icon, contentDescription = null, tint = a.accent, modifier = Modifier.size(36.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(a.title), style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(a.body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(stringResource(R.string.disclaimer_short), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
