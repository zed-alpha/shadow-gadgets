package com.zedalpha.shadowgadgets.demo.topic.compat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.compose.ExperimentalColorCompat
import com.zedalpha.shadowgadgets.compose.shadowCompat
import com.zedalpha.shadowgadgets.demo.databinding.ComposeViewBinding

internal class ComposeIntroPanel(
    inflater: LayoutInflater,
    parent: ViewGroup
) : IntroPanel {

    override val ui = ComposeViewBinding.inflate(inflater, parent, true)

    private var ambientColor by mutableStateOf(DefaultShadowColor)
    override var outlineAmbientShadowColor: Int
        get() = ambientColor.toArgb()
        set(value) {
            ambientColor = Color(value)
        }

    private var spotColor by mutableStateOf(DefaultShadowColor)
    override var outlineSpotShadowColor: Int
        get() = spotColor.toArgb()
        set(value) {
            spotColor = Color(value)
        }

    override var elevation by mutableFloatStateOf(0F)

    override var isShowingBackgrounds by mutableStateOf(true)

    init {
        ui.composeView.apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent { ComposeContent() }
        }
    }

    @OptIn(ExperimentalColorCompat::class)
    @Composable
    private fun ComposeContent() {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = BiasAlignment.Vertical(0.1F),
        ) {
            val background = when {
                isShowingBackgrounds -> Color.White
                else -> Color.Transparent
            }
            val shadowElevation =
                with(LocalDensity.current) { elevation.toDp() }
            val shape = RoundedCornerShape(15.dp)

            Card(
                backgroundColor = background,
                elevation = 0.dp,
                shape = shape,
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = shadowElevation,
                        shape = shape,
                        ambientColor = ambientColor,
                        spotColor = spotColor
                    )
            ) {}

            Card(
                backgroundColor = background,
                elevation = 0.dp,
                shape = shape,
                modifier = Modifier
                    .size(80.dp)
                    .shadowCompat(
                        elevation = shadowElevation,
                        shape = shape,
                        ambientColor = ambientColor,
                        spotColor = spotColor,
                        forceColorCompat = true
                    )
            ) {}
        }
    }
}