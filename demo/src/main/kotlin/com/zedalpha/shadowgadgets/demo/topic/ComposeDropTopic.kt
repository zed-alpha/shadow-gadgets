package com.zedalpha.shadowgadgets.demo.topic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.FloatingActionButtonElevation
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DropShadowScope
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.compose.clippedDropShadow
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.ComposeViewBinding
import com.zedalpha.shadowgadgets.demo.internal.ControlBlue
import com.zedalpha.shadowgadgets.demo.internal.ControlGreen
import com.zedalpha.shadowgadgets.demo.internal.ControlRed
import com.zedalpha.shadowgadgets.demo.internal.GenericCardShape
import com.zedalpha.shadowgadgets.demo.internal.ItemShadowRadius
import com.zedalpha.shadowgadgets.demo.internal.ItemShape
import com.zedalpha.shadowgadgets.demo.internal.ShadowBlue
import com.zedalpha.shadowgadgets.demo.internal.ShadowGreen
import com.zedalpha.shadowgadgets.demo.internal.ShadowRed
import com.zedalpha.shadowgadgets.demo.internal.ZeroButtonElevation
import com.zedalpha.shadowgadgets.demo.internal.ZeroFloatingActionButtonElevation
import com.zedalpha.shadowgadgets.demo.internal.setTopicContent

internal val ComposeDropTopic =
    Topic(
        title = "Compose: Drop",
        descriptionResId = R.string.description_compose_drop,
        fragmentClass = ComposeDropFragment::class.java
    )

class ComposeDropFragment :
    TopicFragment<ComposeViewBinding>(ComposeViewBinding::inflate) {

    override fun loadUi(ui: ComposeViewBinding) =
        ui.composeView.setTopicContent { ComposeDropContent() }
}

@Composable
private fun ComposeDropContent() {
    var clipped by rememberSaveable { mutableStateOf(true) }

    Row(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.5F),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable { clipped = !clipped }
                ) {
                    Switch(clipped, { clipped = it })
                    Text("Clip")
                }
            }

            DropShadowFloatingActionButton(
                clipped = clipped,
                shadowColor = ShadowRed,
                backgroundColor = ControlRed,
                elevation = FloatingActionButtonDefaults
                    .elevation(16.dp, 22.dp, 18.dp, 18.dp)
            )

            DropShadowButton(
                clipped = clipped,
                shadowColor = ShadowGreen,
                colors = ButtonDefaults.buttonColors(ControlGreen),
                elevation = ButtonDefaults
                    .elevation(12.dp, 18.dp, 0.dp, 14.dp, 14.dp)
            )

            Card(
                shape = GenericCardShape,
                backgroundColor = ControlBlue,
                elevation = 0.dp,
                modifier = Modifier
                    .size(80.dp)
                    .selectDropShadow(clipped, GenericCardShape) {
                        radius = ItemShadowRadius.toPx()
                        color = ShadowBlue
                    }
            ) {}
        }

        ColorfulLazyColumn(
            modifier = Modifier
                .weight(0.5F)
                .fillMaxHeight()
                .padding(start = 10.dp, end = 20.dp),
            shadowModifier = { color ->
                selectDropShadow(clipped, ItemShape) {
                    radius = ItemShadowRadius.toPx()
                    this.color = color
                }
            }
        )
    }
}

private fun Modifier.selectDropShadow(
    clipped: Boolean,
    shape: Shape,
    block: DropShadowScope.() -> Unit
): Modifier =
    if (clipped) clippedDropShadow(shape, block) else dropShadow(shape, block)

@Composable
private fun DropShadowFloatingActionButton(
    clipped: Boolean,
    shadowColor: Color,
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    backgroundColor: Color = MaterialTheme.colors.secondary,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val elevationDp by elevation.elevation(interactionSource)

    FloatingActionButton(
        onClick = {},
        modifier = Modifier.selectDropShadow(clipped, shape) {
            radius = elevationDp.toPx()
            color = shadowColor
        },
        interactionSource = interactionSource,
        shape = shape,
        backgroundColor = backgroundColor,
        elevation = ZeroFloatingActionButtonElevation,
        content = {}
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DropShadowButton(
    clipped: Boolean,
    shadowColor: Color,
    elevation: ButtonElevation = ButtonDefaults.elevation(),
    shape: Shape = MaterialTheme.shapes.small,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val elevationDp by elevation.elevation(true, interactionSource)

    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Button(
            onClick = {},
            modifier = Modifier.selectDropShadow(clipped, shape) {
                radius = elevationDp.toPx()
                color = shadowColor
            },
            interactionSource = interactionSource,
            elevation = ZeroButtonElevation,
            shape = shape,
            colors = colors,
            content = {}
        )
    }
}