package io.github.junaaid96.colorvision.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.color.ColorMath
import io.github.junaaid96.colorvision.color.CvdType

/** Real-time view filter: none, simulate a deficiency, or correct (daltonize) for it. */
data class ViewFilter(val type: CvdType? = null, val correct: Boolean = false, val severity: Float = 1f) {
    fun colorFilter(): ColorFilter? = type?.let {
        ColorFilter.colorMatrix(ColorMatrix(ColorMath.androidColorMatrix(it, severity.toDouble(), correct)))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterControls(filter: ViewFilter, onChange: (ViewFilter) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter.type == null, onClick = { onChange(filter.copy(type = null)) }, label = { Text(stringResource(R.string.filter_none)) })
            CvdType.entries.forEach { t ->
                FilterChip(selected = filter.type == t, onClick = { onChange(filter.copy(type = t)) }, label = { Text(stringResource(t.label())) })
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !filter.correct, onClick = { onChange(filter.copy(correct = false)) }, label = { Text(stringResource(R.string.filter_simulate)) })
            FilterChip(selected = filter.correct, onClick = { onChange(filter.copy(correct = true)) }, label = { Text(stringResource(R.string.filter_correct)) })
        }
        val sevLabel = stringResource(R.string.filter_severity, (filter.severity * 100).toInt())
        Text(sevLabel)
        Slider(value = filter.severity, onValueChange = { onChange(filter.copy(severity = it)) }, modifier = Modifier.semantics { contentDescription = sevLabel })
    }
}
