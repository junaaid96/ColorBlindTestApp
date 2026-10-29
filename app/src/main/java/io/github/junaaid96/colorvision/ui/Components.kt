package io.github.junaaid96.colorvision.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.junaaid96.colorvision.R
import io.github.junaaid96.colorvision.tests.Plate

/** Standard screen frame: collapsing large app bar, optional back, content centred on wide screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenFrame(title: String, onBack: (() -> Unit)? = null, content: @Composable BoxScope.(PaddingValues) -> Unit) {
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                scrollBehavior = scroll,
            )
        },
    ) { pad ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.widthIn(max = 840.dp).fillMaxSize()) { content(pad) }
        }
    }
}

/** Draws a generated plate. The dots are pre-computed so drawing is cheap. */
@Composable
fun PlateView(plate: Plate, modifier: Modifier = Modifier) {
    val cd = stringResource(R.string.plate_cd, plate.id)
    Canvas(
        modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .semantics { contentDescription = cd },
    ) {
        val s = size.minDimension
        drawCircle(Color(0xFFF4F1EA), radius = s / 2)
        plate.dots.forEach { d ->
            drawCircle(Color(d.argb), radius = d.r * s, center = Offset(d.x * s, d.y * s))
        }
    }
}
