package com.zedalpha.shadowgadgets.demo.topic

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.compose.clippedShadow
import com.zedalpha.shadowgadgets.compose.shadowCompat
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.ComposeViewBinding
import com.zedalpha.shadowgadgets.demo.internal.ControlBlue
import com.zedalpha.shadowgadgets.demo.internal.GenericCardShape
import com.zedalpha.shadowgadgets.demo.internal.ItemElevation
import com.zedalpha.shadowgadgets.demo.internal.ShadowBlue
import com.zedalpha.shadowgadgets.demo.internal.State
import com.zedalpha.shadowgadgets.demo.internal.setTopicContent

internal val ComposeLambdaTopic =
    Topic(
        title = "Compose: Lambda",
        descriptionResId = R.string.description_compose_lambda,
        fragmentClass = ComposeBlockFragment::class.java
    )

class ComposeBlockFragment :
    TopicFragment<ComposeViewBinding>(ComposeViewBinding::inflate) {

    override fun loadUi(ui: ComposeViewBinding) =
        ui.composeView.setTopicContent { ComposeBlockContent() }
}

@Composable
private fun ComposeBlockContent() {
    var animate by rememberSaveable { mutableStateOf(false) }
    var useLambda by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            AnimatedClippedShadow(animate, useLambda)

            Spacer(Modifier.size(40.dp))

            AnimatedShadowCompat(animate, useLambda)
        }

        Row {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable { animate = !animate }
            ) {
                Switch(animate, { animate = it })
                Text("Animate")
            }

            Spacer(Modifier.size(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable { useLambda = !useLambda }
            ) {
                Switch(useLambda, { useLambda = it })
                Text("Use lambda")
            }

            Spacer(Modifier.size(20.dp))
        }
    }
}

@Composable
private fun AnimatedClippedShadow(animate: Boolean, useLambda: Boolean) {
    val elevation: Dp
            by if (animate) {
                rememberInfiniteTransition().animateValue(
                    initialValue = ItemElevation,
                    targetValue = 1.dp,
                    typeConverter = Dp.VectorConverter,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        )
                )
            } else {
                // Probably not advisable, but it works for a demo.
                State(ItemElevation)
            }

    Box(
        modifier = Modifier
            .size(100.dp)
            .run {
                if (useLambda) {
                    clippedShadow(GenericCardShape) {
                        this.elevation = elevation.toPx()
                        colorCompat = ShadowBlue
                        forceColorCompat = true
                    }
                } else {
                    clippedShadow(
                        elevation = elevation,
                        shape = GenericCardShape,
                        colorCompat = ShadowBlue,
                        forceColorCompat = true
                    )
                }
            }
            .background(ControlBlue, GenericCardShape)
    )
}

@Composable
private fun AnimatedShadowCompat(animate: Boolean, useLambda: Boolean) {
    val color: Color
            by if (animate) {
                rememberInfiniteTransition().animateColor(
                    initialValue = ShadowBlue,
                    targetValue = ShadowBlue,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1000
                            ShadowBlue at 0
                            Color.Red at 333
                            Color.Green at 667
                            Color.Blue at 1000
                        },
                        repeatMode = RepeatMode.Reverse
                    ),
                )
            } else {
                // Probably not advisable, but it works for a demo.
                State(ShadowBlue)
            }

    Box(
        modifier = Modifier
            .size(100.dp)
            .run {
                if (useLambda) {
                    shadowCompat(GenericCardShape) {
                        this.elevation = ItemElevation.toPx()
                        colorCompat = color
                        forceColorCompat = true
                    }
                } else {
                    shadowCompat(
                        elevation = ItemElevation,
                        shape = GenericCardShape,
                        colorCompat = color,
                        forceColorCompat = true
                    )
                }
            }
            .background(Color(0xffddeeff), GenericCardShape)
    )
}