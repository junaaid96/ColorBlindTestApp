package io.github.junaaid96.colorvision.feature

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.color.ColorNamer
import io.github.junaaid96.colorvision.color.Rgb
import io.github.junaaid96.colorvision.ui.AppViewModel
import io.github.junaaid96.colorvision.ui.ScreenFrame
import java.util.concurrent.Executors

/**
 * Color Lens: live camera with a crosshair that names the real-world color (sampled before
 * any filter) and optional simulation/correction filters. Frames never leave the device.
 */
@Composable
fun LensScreen(vm: AppViewModel) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    ScreenFrame(stringResource(R.string.nav_lens)) { pad ->
        if (!granted) {
            Column(Modifier.padding(pad).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.lens_permission), style = MaterialTheme.typography.bodyLarge)
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text(stringResource(R.string.action_allow_camera)) }
            }
        } else LensContent(vm, Modifier.padding(pad))
    }
}

@Composable
private fun LensContent(vm: AppViewModel, modifier: Modifier) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val speaker = rememberSpeaker()
    var frame by remember { mutableStateOf<Bitmap?>(null) }
    var frozen by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(ViewFilter()) }
    val frozenState by rememberUpdatedState(frozen)

    DisposableEffect(owner) {
        val executor = Executors.newSingleThreadExecutor()
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            val analysis = ImageAnalysis.Builder()
                .setResolutionSelector(ResolutionSelector.Builder().setResolutionStrategy(ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)).build())
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(executor) { proxy ->
                if (!frozenState) {
                    val raw = proxy.toBitmap()
                    val rot = proxy.imageInfo.rotationDegrees
                    frame = if (rot == 0) raw else Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, Matrix().apply { postRotate(rot.toFloat()) }, true)
                }
                proxy.close()
            }
            provider.unbindAll()
            provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, analysis)
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            runCatching { future.get().unbindAll() }
            executor.shutdown()
        }
    }

    val bmp = frame
    val sample = bmp?.let { b ->
        // Average a 5x5 patch at the centre to reduce sensor noise.
        var r = 0; var g = 0; var bl = 0; var n = 0
        for (dx in -2..2) for (dy in -2..2) {
            val p = b.getPixel((b.width / 2 + dx).coerceIn(0, b.width - 1), (b.height / 2 + dy).coerceIn(0, b.height - 1))
            r += (p shr 16) and 0xFF; g += (p shr 8) and 0xFF; bl += p and 0xFF; n++
        }
        Rgb(r / n / 255.0, g / n / 255.0, bl / n / 255.0)
    }
    val named = sample?.let { ColorNamer.name(it) }
    LaunchedEffect(named?.name, frozen) {
        if (named != null && frozen && settings?.speech == true) speaker.say(named.name)
    }

    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f).fillMaxWidth().background(Color.Black, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
            if (bmp != null) Image(bmp.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Fit, colorFilter = filter.colorFilter(), modifier = Modifier.fillMaxSize())
            Canvas(Modifier.size(56.dp)) {
                drawCircle(Color.White, radius = size.minDimension / 2, style = Stroke(4f))
                drawCircle(Color.Black, radius = size.minDimension / 2 - 4f, style = Stroke(2f))
                drawLine(Color.White, Offset(center.x, 0f), Offset(center.x, size.height * 0.3f), 3f)
                drawLine(Color.White, Offset(center.x, size.height * 0.7f), Offset(center.x, size.height), 3f)
            }
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(48.dp).background(Color(sample?.toArgb() ?: 0xFF808080.toInt()), RoundedCornerShape(12.dp)))
                Column(Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite }) {
                    Text(named?.name ?: "…", style = MaterialTheme.typography.titleLarge)
                    if (sample != null && named != null) Text(stringResource(R.string.lens_detail, sample.hex(), (ColorNamer.confidence(named) * 100).toInt()), style = MaterialTheme.typography.bodySmall)
                }
                FilledTonalIconButton(onClick = { named?.let { speaker.say(it.name); haptic.performHapticFeedback(HapticFeedbackType.Confirm) } }) {
                    Icon(Icons.Outlined.RecordVoiceOver, stringResource(R.string.lens_speak))
                }
                FilledTonalIconButton(onClick = { frozen = !frozen }) {
                    Icon(if (frozen) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, stringResource(if (frozen) R.string.lens_resume else R.string.lens_freeze))
                }
            }
        }
        FilterControls(filter, { filter = it })
    }
}
