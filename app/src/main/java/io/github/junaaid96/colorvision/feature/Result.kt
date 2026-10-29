package io.github.junaaid96.colorvision.feature

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.data.TestKind
import io.github.junaaid96.colorvision.tests.ScreeningResult
import io.github.junaaid96.colorvision.ui.AppViewModel
import io.github.junaaid96.colorvision.ui.ScreenFrame
import io.github.junaaid96.colorvision.ui.theme.Safe
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun ResultScreen(vm: AppViewModel, onDone: () -> Unit, onRetake: (Boolean) -> Unit) {
    val last by vm.lastResult.collectAsStateWithLifecycle()
    val current = last
    if (current == null) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onDone() }
        return
    }
    val (kind, r) = current
    val context = LocalContext.current
    val summary = summaryText(context, kind, r)
    ScreenFrame(stringResource(R.string.result_title), onBack = onDone) { pad ->
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding(), bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stringResource(r.classification.label()), style = MaterialTheme.typography.displaySmall, modifier = Modifier.semantics { heading() })
                        Text(stringResource(r.classification.description()), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.result_severity, stringResource(r.severity.label())), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.result_confidence, (r.confidence * 100).toInt()), style = MaterialTheme.typography.titleMedium)
                        LinearProgressIndicator(progress = { r.confidence.toFloat() }, modifier = Modifier.fillMaxWidth())
                        Text(stringResource(R.string.result_score, r.score), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.disclaimer_short), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { shareText(context, summary) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Share, contentDescription = null); Text(stringResource(R.string.action_share), Modifier.padding(start = 8.dp))
                    }
                    OutlinedButton(onClick = { sharePdf(context, summary) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null); Text(stringResource(R.string.action_pdf), Modifier.padding(start = 8.dp))
                    }
                }
            }
            item { Text(stringResource(R.string.result_breakdown), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() }) }
            items(r.outcomes) { o ->
                ListItem(
                    leadingContent = {
                        // Shape and text, not only color, carry the meaning.
                        if (o.correct) Icon(Icons.Outlined.CheckCircle, stringResource(R.string.result_correct), tint = Safe.blue)
                        else Icon(Icons.Outlined.Close, stringResource(R.string.result_missed), tint = Safe.vermillion)
                    },
                    headlineContent = { Text(stringResource(R.string.result_plate_row, o.plate, o.expected, o.given.ifEmpty { stringResource(R.string.answer_nothing) })) },
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { onRetake(kind == TestKind.FULL) }) { Text(stringResource(R.string.action_retake)) }
                    Button(onClick = onDone) { Text(stringResource(R.string.action_done)) }
                }
            }
        }
    }
}

fun summaryText(context: Context, kind: TestKind, r: ScreeningResult): String = buildString {
    appendLine(context.getString(R.string.app_name) + " · " + context.getString(kind.label()))
    appendLine(DateFormat.getDateTimeInstance().format(Date()))
    appendLine(context.getString(r.classification.label()))
    appendLine(context.getString(R.string.result_severity, context.getString(r.severity.label())))
    appendLine(context.getString(R.string.result_confidence, (r.confidence * 100).toInt()))
    appendLine(context.getString(R.string.result_score, r.score))
    appendLine()
    r.outcomes.forEach {
        appendLine(context.getString(R.string.result_plate_row, it.plate, it.expected, it.given.ifEmpty { context.getString(R.string.answer_nothing) }))
    }
    appendLine()
    append(context.getString(R.string.disclaimer_short))
}

fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(send, null))
}

/** Renders the summary to a one-page PDF in the cache and opens the share sheet. */
fun sharePdf(context: Context, text: String) {
    val doc = PdfDocument()
    val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val paint = Paint().apply { textSize = 12f; isAntiAlias = true }
    val title = Paint(paint).apply { textSize = 20f; isFakeBoldText = true }
    var y = 60f
    text.lines().forEachIndexed { i, line ->
        page.canvas.drawText(line, 48f, y, if (i == 0) title else paint)
        y += if (i == 0) 30f else 18f
    }
    doc.finishPage(page)
    val dir = File(context.cacheDir, "reports").apply { mkdirs() }
    val file = File(dir, "color-vision-report.pdf")
    file.outputStream().use { doc.writeTo(it) }
    doc.close()
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val send = Intent(Intent.ACTION_SEND).setType("application/pdf").putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, null))
}
