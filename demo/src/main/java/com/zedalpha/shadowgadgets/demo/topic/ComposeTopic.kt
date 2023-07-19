package com.zedalpha.shadowgadgets.demo.topic

import android.os.Build
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.compose.clippedShadow
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentComposeBinding


internal object ComposeTopic : Topic {

    override val title = "Compose"

    override val descriptionResId = R.string.description_compose

    override fun createContentFragment() = Content()

    class Content : ContentFragment(R.layout.fragment_compose) {

        override fun loadUi(view: View) {
            val ui = FragmentComposeBinding.bind(view)

            ui.composeView.apply {
                setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
                setContent { ComposeContent() }
            }
        }
    }
}

@Composable
private fun ComposeContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClippedShadowFloatingActionButton(
                onClick = {},
                backgroundColor = Color(0x22ff4444),
                elevation = FloatingActionButtonDefaults.elevation(
                    16.dp,
                    22.dp,
                    18.dp,
                    18.dp
                ),
                shadowAmbientColor = Color(0xffff4444),
                shadowSpotColor = Color(0xffff4444)
            ) {}

            ClippedShadowButton(
                onClick = {},
                colors = ButtonDefaults.buttonColors(Color(0x357fcc7f)),
                elevation = ButtonDefaults.elevation(
                    12.dp,
                    18.dp,
                    0.dp,
                    14.dp,
                    14.dp
                ),
                shadowAmbientColor = Color(0xff448866),
                shadowSpotColor = Color(0xff448866)
            ) {}
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val cardShape = GenericShape(CardShapeBuilder)
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .clippedShadow(
                        elevation = 10.dp,
                        shape = cardShape,
                        ambientColor = Color(0xff007fff),
                        spotColor = Color(0xff007fff)
                    ),
                shape = cardShape,
                backgroundColor = Color(0x22007fff),
                elevation = 0.dp,
            ) {}
        }
    }
}

@Composable
private fun ClippedShadowFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    backgroundColor: Color = MaterialTheme.colors.secondary,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    shadowAmbientColor: Color = DefaultShadowColor,
    shadowSpotColor: Color = DefaultShadowColor,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.clippedShadow(
            elevation = elevation.elevation(interactionSource).value,
            shape = shape,
            ambientColor = shadowAmbientColor,
            spotColor = shadowSpotColor,
        ),
        interactionSource = interactionSource,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults
            .elevation(0.dp, 0.dp, 0.dp, 0.dp),
        content = content
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClippedShadowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape: Shape = MaterialTheme.shapes.small,
    shadowAmbientColor: Color = DefaultShadowColor,
    shadowSpotColor: Color = DefaultShadowColor,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Button(
            onClick = onClick,
            modifier = modifier.clippedShadow(
                elevation = elevation?.elevation(
                    enabled,
                    interactionSource
                )?.value ?: 0.dp,
                shape = shape,
                ambientColor = shadowAmbientColor,
                spotColor = shadowSpotColor
            ),
            enabled = enabled,
            interactionSource = interactionSource,
            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            shape = shape,
            border = border,
            colors = colors,
            contentPadding = contentPadding,
            content = content
        )
    }
}

private val PuzzlePieceBuilder: Path.(Size, LayoutDirection) -> Unit =
    { size, _ ->
        val side = minOf(size.width, size.height)
        translate(Offset((size.width - side) / 2, (size.height - side) / 2))

        val q = side / 4
        // top
        moveTo(0F, q)
        lineTo(q, q)
        arcTo(Rect(q, 0F, 2 * q, q), 100F, 340F, false)
        lineTo(2 * q, q)
        lineTo(3 * q, q)

        // right
        lineTo(3 * q, 2 * q)
        arcTo(Rect(3 * q, 2 * q, 4 * q, 3 * q), 190F, 340F, false)
        lineTo(3 * q, 3 * q)
        lineTo(3 * q, 4 * q)

        // bottom
        lineTo(2 * q, 4 * q)
        arcTo(Rect(q, 3 * q, 2 * q, 4 * q), 80F, -340F, false)
        lineTo(q, 4 * q)
        lineTo(0F, 4 * q)

        // left
        lineTo(0F, 3 * q)
        arcTo(Rect(0F, 2 * q, q, 3 * q), 170F, -340F, false)
        lineTo(0F, 2 * q)
        lineTo(0F, q)

        close()
    }

private val CompassPointerBuilder: Path.(Size, LayoutDirection) -> Unit =
    { size, _ ->
        val side = minOf(size.width, size.height)
        translate(Offset((size.width - side) / 2, (size.height - side) / 2))

        val h = side / 2
        reset()
        addRoundRect(
            RoundRect(
                0F,
                0F,
                side,
                side,
                topRightCornerRadius = CornerRadius(h),
                bottomLeftCornerRadius = CornerRadius(h)
            )
        )
    }

private val CardShapeBuilder =
    if (Build.VERSION.SDK_INT >= 30) {
        PuzzlePieceBuilder
    } else {
        CompassPointerBuilder
    }