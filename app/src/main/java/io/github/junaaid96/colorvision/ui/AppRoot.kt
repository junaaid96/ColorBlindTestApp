package io.github.junaaid96.colorvision.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.data.TestKind
import io.github.junaaid96.colorvision.feature.ArrangementScreen
import io.github.junaaid96.colorvision.feature.ContrastScreen
import io.github.junaaid96.colorvision.feature.HistoryScreen
import io.github.junaaid96.colorvision.feature.HomeScreen
import io.github.junaaid96.colorvision.feature.LensScreen
import io.github.junaaid96.colorvision.feature.OnboardingScreen
import io.github.junaaid96.colorvision.feature.PlateTestScreen
import io.github.junaaid96.colorvision.feature.PracticeScreen
import io.github.junaaid96.colorvision.feature.ResultScreen
import io.github.junaaid96.colorvision.feature.SettingsScreen
import io.github.junaaid96.colorvision.feature.SimulatorScreen
import io.github.junaaid96.colorvision.ui.theme.ColorVisionTheme

object Routes {
    const val HOME = "home"
    const val LENS = "lens"
    const val SIMULATE = "simulate"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val ONBOARDING = "onboarding"
    const val PLATES = "plates/{full}"
    const val RESULT = "result"
    const val ARRANGE = "arrange"
    const val CONTRAST = "contrast"
    const val PRACTICE = "practice"
    fun plates(full: Boolean) = "plates/$full"
}

private enum class Top(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    HOME(Routes.HOME, R.string.nav_home, Icons.Outlined.Home),
    LENS(Routes.LENS, R.string.nav_lens, Icons.Outlined.CameraAlt),
    SIMULATE(Routes.SIMULATE, R.string.nav_simulate, Icons.Outlined.Visibility),
    HISTORY(Routes.HISTORY, R.string.nav_history, Icons.Outlined.Insights),
    SETTINGS(Routes.SETTINGS, R.string.nav_settings, Icons.Outlined.Settings),
}

@Composable
fun AppRoot(vm: AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val s = settings ?: return // DataStore still loading; splash stays on screen briefly.
    ColorVisionTheme(s) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val nav = rememberNavController()
            val entry by nav.currentBackStackEntryAsState()
            val route = entry?.destination?.route
            val isTop = Top.entries.any { it.route == route }
            val adaptive = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
            NavigationSuiteScaffold(
                layoutType = if (isTop) adaptive else NavigationSuiteType.None,
                navigationSuiteItems = {
                    Top.entries.forEach { t ->
                        item(
                            selected = route == t.route,
                            onClick = { nav.goTop(t.route) },
                            icon = { Icon(t.icon, contentDescription = null) },
                            label = { Text(stringResource(t.label)) },
                        )
                    }
                },
            ) { Graph(nav, vm, if (s.onboarded) Routes.HOME else Routes.ONBOARDING) }
        }
    }
}

private fun NavHostController.goTop(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
private fun Graph(nav: NavHostController, vm: AppViewModel, start: String) {
    NavHost(nav, startDestination = start) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = {
                vm.updateSettings { it.copy(onboarded = true) }
                nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(vm, onOpen = { nav.navigate(it) })
        }
        composable(Routes.LENS) { LensScreen(vm) }
        composable(Routes.SIMULATE) { SimulatorScreen() }
        composable(Routes.HISTORY) { HistoryScreen(vm) }
        composable(Routes.SETTINGS) {
            SettingsScreen(vm, onReplayOnboarding = { nav.navigate(Routes.ONBOARDING) })
        }
        composable(Routes.PLATES) { back ->
            val full = back.arguments?.getString("full") == "true"
            PlateTestScreen(full = full, onBack = { nav.popBackStack() }, onFinished = { result ->
                vm.saveScreening(if (full) TestKind.FULL else TestKind.QUICK, result)
                nav.navigate(Routes.RESULT) { popUpTo(Routes.HOME) }
            })
        }
        composable(Routes.RESULT) {
            ResultScreen(vm, onDone = { nav.popBackStack(Routes.HOME, inclusive = false) }, onRetake = { full ->
                nav.navigate(Routes.plates(full)) { popUpTo(Routes.HOME) }
            })
        }
        composable(Routes.ARRANGE) { ArrangementScreen(vm, onBack = { nav.popBackStack() }) }
        composable(Routes.CONTRAST) { ContrastScreen(vm, onBack = { nav.popBackStack() }) }
        composable(Routes.PRACTICE) { PracticeScreen(vm, onBack = { nav.popBackStack() }) }
    }
}
