package io.github.junaaid96.colorvision.feature

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.color.CvdType
import io.github.junaaid96.colorvision.ui.ScreenFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Pick any photo and see it side by side as people with each deficiency would. */
@Composable
fun SimulatorScreen() {
    val context = LocalContext.current
    var uri by remember { mutableStateOf<Uri?>(null) }
    var bmp by remember { mutableStateOf<Bitmap?>(null) }
    var severity by remember { mutableFloatStateOf(1f) }
    var correct by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri = it ?: uri }
    LaunchedEffect(uri) {
        val u = uri ?: return@LaunchedEffect
        bmp = withContext(Dispatchers.IO) { decode(context, u) }
    }
    ScreenFrame(stringResource(R.string.nav_simulate)) { pad ->
        LazyVerticalGrid(GridCells.Adaptive(220.dp), Modifier.padding(pad), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.sim_intro), style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text(stringResource(R.string.sim_pick)) }
                    Text(stringResource(R.string.filter_severity, (severity * 100).toInt()))
                    Slider(severity, { severity = it })
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Switch(correct, { correct = it }); Text(stringResource(R.string.filter_correct))
                    }
                }
            }
            val b = bmp
            if (b != null) {
                val variants: List<CvdType?> = listOf(null) + CvdType.entries
                items(variants) { t ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Image(b.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop,
                            colorFilter = ViewFilter(t, correct, severity).colorFilter(),
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)))
                        Text(if (t == null) stringResource(R.string.filter_none) else stringResource(t.label()), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

private fun decode(context: android.content.Context, uri: Uri): Bitmap? = runCatching {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    var sample = 1
    while (maxOf(opts.outWidth, opts.outHeight) / sample > 1024) sample *= 2
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
}.getOrNull()
