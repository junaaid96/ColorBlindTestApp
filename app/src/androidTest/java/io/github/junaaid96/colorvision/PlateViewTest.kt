package io.github.junaaid96.colorvision

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.junaaid96.colorvision.tests.PlateGenerator
import io.github.junaaid96.colorvision.ui.PlateView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlateViewTest {
    @get:Rule val rule = createComposeRule()

    @Test fun plateRenders() {
        val plate = PlateGenerator.plan(false).first()
        rule.setContent { Column { Text("plate"); PlateView(plate) } }
        rule.onNodeWithText("plate").assertIsDisplayed()
    }
}
