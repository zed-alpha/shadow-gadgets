package com.zedalpha.shadowgadgets.demo.topic

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zedalpha.shadowgadgets.compose.clippedShadow
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.ComposeViewBinding
import com.zedalpha.shadowgadgets.demo.internal.ControlBlue
import com.zedalpha.shadowgadgets.demo.internal.ControlGreen
import com.zedalpha.shadowgadgets.demo.internal.ControlRed
import com.zedalpha.shadowgadgets.demo.internal.GenericCardShape
import com.zedalpha.shadowgadgets.demo.internal.ItemBlue
import com.zedalpha.shadowgadgets.demo.internal.ItemElevation
import com.zedalpha.shadowgadgets.demo.internal.ItemGreen
import com.zedalpha.shadowgadgets.demo.internal.ItemRed
import com.zedalpha.shadowgadgets.demo.internal.ItemShape
import com.zedalpha.shadowgadgets.demo.internal.ShadowBlue
import com.zedalpha.shadowgadgets.demo.internal.ShadowGreen
import com.zedalpha.shadowgadgets.demo.internal.ShadowRed
import com.zedalpha.shadowgadgets.demo.internal.ZeroButtonElevation
import com.zedalpha.shadowgadgets.demo.internal.ZeroFloatingActionButtonElevation
import com.zedalpha.shadowgadgets.demo.internal.setTopicContent

internal val ComposeIntroTopic =
    Topic(
        title = "Compose: Intro",
        descriptionResId = R.string.description_compose_intro,
        fragmentClass = ComposeIntroFragment::class.java
    )

class ComposeIntroFragment :
    TopicFragment<ComposeViewBinding>(ComposeViewBinding::inflate) {

    override fun loadUi(ui: ComposeViewBinding) =
        ui.composeView.setTopicContent { ComposeIntroContent() }
}

@Composable
private fun ComposeIntroContent() {
    Row(modifier = Modifier.fillMaxSize()) {

        ColorfulLazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.6F)
                .padding(start = 20.dp, end = 10.dp),
            shadowModifier = { clippedShadow(ItemElevation, ItemShape) }
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.4F),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ClippedShadowFloatingActionButton(
                backgroundColor = ControlRed,
                elevation = FloatingActionButtonDefaults
                    .elevation(16.dp, 22.dp, 18.dp, 18.dp),
                shadowColor = ShadowRed
            )

            ClippedShadowButton(
                colors = ButtonDefaults.buttonColors(ControlGreen),
                elevation = ButtonDefaults
                    .elevation(12.dp, 18.dp, 0.dp, 14.dp, 14.dp),
                shadowColor = ShadowGreen
            )

            Card(
                shape = GenericCardShape,
                backgroundColor = ControlBlue,
                elevation = 0.dp,
                modifier = Modifier
                    .size(80.dp)
                    .clippedShadow(
                        elevation = ItemElevation,
                        shape = GenericCardShape,
                        ambientColor = ShadowBlue,
                        spotColor = ShadowBlue
                    )
            ) {}
        }
    }
}

@Composable
internal fun ColorfulLazyColumn(
    modifier: Modifier,
    shadowModifier: Modifier.(Color) -> Modifier
) {
    val textStyle = LocalTextStyle.current.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = true)
    )
    CompositionLocalProvider(LocalTextStyle provides textStyle) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = modifier
        ) {
            items(ItemCount) { position ->
                val clearColor = lerp(
                    if (position < HalfItemCount) ItemRed else ItemGreen,
                    if (position < HalfItemCount) ItemGreen else ItemBlue,
                    (position % HalfItemCount).toFloat() / HalfItemCount
                )
                Card(
                    shape = ItemShape,
                    backgroundColor = clearColor,
                    elevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadowModifier(clearColor.copy(alpha = 1F))
                ) {
                    Text(
                        text = "Item $position",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClippedShadowFloatingActionButton(
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    backgroundColor: Color = MaterialTheme.colors.secondary,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    shadowColor: Color = DefaultShadowColor
) {
    val interactionSource = remember { MutableInteractionSource() }
    val elevationDp by elevation.elevation(interactionSource)

    FloatingActionButton(
        onClick = {},
        modifier = Modifier.clippedShadow(shape) {
            this.elevation = elevationDp.toPx()
            ambientColor = shadowColor
            spotColor = shadowColor
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
private fun ClippedShadowButton(
    elevation: ButtonElevation = ButtonDefaults.elevation(),
    shape: Shape = MaterialTheme.shapes.small,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    shadowColor: Color = DefaultShadowColor
) {
    val interactionSource = remember { MutableInteractionSource() }
    val elevationDp by elevation.elevation(true, interactionSource)

    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Button(
            onClick = {},
            modifier = Modifier.clippedShadow(shape) {
                this.elevation = elevationDp.toPx()
                ambientColor = shadowColor
                spotColor = shadowColor
            }, interactionSource = interactionSource,
            elevation = ZeroButtonElevation,
            shape = shape,
            colors = colors,
            content = {}
        )
    }
}