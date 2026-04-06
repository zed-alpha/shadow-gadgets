package com.zedalpha.shadowgadgets.demo.internal

import android.os.Build
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonElevation
import androidx.compose.material.FloatingActionButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.demo.topic.ItemBlueInt
import com.zedalpha.shadowgadgets.demo.topic.ItemGreenInt
import com.zedalpha.shadowgadgets.demo.topic.ItemRedInt

internal fun ComposeView.setTopicContent(content: @Composable (() -> Unit)) {
    this.setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
    this.setContent(content)
}

// Probably not advisable, but it works for the demo.
internal fun <T> State(value: T): State<T> = StateImpl(value)
private class StateImpl<T>(override val value: T) : State<T>

internal val ZeroButtonElevation =
    object : ButtonElevation {
        @Composable
        override fun elevation(
            enabled: Boolean,
            interactionSource: InteractionSource
        ): State<Dp> =
            State(0.dp)
    }

internal val ZeroFloatingActionButtonElevation =
    object : FloatingActionButtonElevation {
        @Composable
        override fun elevation(interactionSource: InteractionSource): State<Dp> =
            State(0.dp)
    }

internal val ItemElevation = 15.dp
internal val ItemShape = RoundedCornerShape(6.dp)
internal val ItemShadowRadius = 8.dp

internal val ControlRed = Color(0x22ff4444)
internal val ControlGreen = Color(0x357fcc7f)
internal val ControlBlue = Color(0x22007fff)
internal val ItemRed = Color(ItemRedInt)
internal val ItemGreen = Color(ItemGreenInt)
internal val ItemBlue = Color(ItemBlueInt)
internal val ShadowRed = Color(0xffff4444)
internal val ShadowGreen = Color(0xff448866)
internal val ShadowBlue = Color(0xff007fff)

private fun puzzlePieceBuilder(): Path.(Size, LayoutDirection) -> Unit =
    { size, _ ->
        rewind()

        val side = minOf(size.width, size.height)
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

        translate(Offset((size.width - side) / 2, (size.height - side) / 2))
    }

private fun compassPointerBuilder(): Path.(Size, LayoutDirection) -> Unit =
    { size, _ ->
        rewind()

        val side = minOf(size.width, size.height)

        addRoundRect(
            RoundRect(
                0F, 0F,
                side, side,
                topRightCornerRadius = CornerRadius(side / 2),
                bottomLeftCornerRadius = CornerRadius(side / 2)
            )
        )

        translate(Offset((size.width - side) / 2, (size.height - side) / 2))
    }

internal val GenericCardShape: GenericShape =
    GenericShape(
        if (Build.VERSION.SDK_INT >= 30) {
            puzzlePieceBuilder()
        } else {
            compassPointerBuilder()
        }
    )