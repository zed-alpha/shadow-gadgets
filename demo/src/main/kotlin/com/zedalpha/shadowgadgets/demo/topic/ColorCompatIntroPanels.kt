package com.zedalpha.shadowgadgets.demo.topic

import android.os.Build
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
import androidx.compose.ui.unit.dp
import androidx.viewbinding.ViewBinding
import com.zedalpha.shadowgadgets.compose.shadowCompat
import com.zedalpha.shadowgadgets.demo.databinding.ComposeViewBinding
import com.zedalpha.shadowgadgets.demo.databinding.ViewPanelBinding
import com.zedalpha.shadowgadgets.demo.internal.setTopicContent
import com.zedalpha.shadowgadgets.demo.internal.toColorStateList
import com.zedalpha.shadowgadgets.view.ShadowColorsBlender
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane
import android.graphics.Color as AndroidColor

internal interface ColorCompatIntroPanel {
    val ui: ViewBinding
    var isShowingBackgrounds: Boolean
    var outlineAmbientShadowColor: Int
    var outlineSpotShadowColor: Int
    var elevation: Float
}

internal class ViewColorCompatIntroPanel(
    inflater: LayoutInflater,
    parent: ViewGroup
) : ColorCompatIntroPanel {

    private val blender = ShadowColorsBlender(parent.context)

    override val ui = ViewPanelBinding.inflate(inflater, parent, true)

    init {
        ui.compat.shadowPlane = ShadowPlane.Background
        ui.compat.forceOutlineShadowColorCompat = true
    }

    override var isShowingBackgrounds: Boolean
        get() = ui.normal.backgroundTintList == null
        set(isShown) {
            val csl =
                (if (isShown) AndroidColor.WHITE else AndroidColor.TRANSPARENT)
                    .toColorStateList()
            ui.normal.backgroundTintList = csl
            ui.compat.backgroundTintList = csl
        }

    override var elevation: Float
        get() = ui.normal.elevation
        set(value) {
            ui.normal.elevation = value
            ui.compat.elevation = value
        }

    override var outlineAmbientShadowColor: Int = AndroidColor.BLACK
        set(value) {
            if (field == value) return
            field = value
            if (Build.VERSION.SDK_INT >= 28) {
                ui.normal.outlineAmbientShadowColor = value
            }
            updateColorCompat()
        }

    override var outlineSpotShadowColor: Int = AndroidColor.BLACK
        set(value) {
            if (field == value) return
            field = value
            if (Build.VERSION.SDK_INT >= 28) {
                ui.normal.outlineSpotShadowColor = value
            }
            updateColorCompat()
        }

    private fun updateColorCompat() {
        ui.compat.outlineShadowColorCompat =
            blender.blend(outlineAmbientShadowColor, outlineSpotShadowColor)
    }
}

internal class ComposeColorCompatIntroPanel(
    inflater: LayoutInflater,
    parent: ViewGroup
) : ColorCompatIntroPanel {

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
        ui.composeView.setTopicContent { ComposeIntroContent() }
    }

    @Composable
    private fun ComposeIntroContent() {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = BiasAlignment.Vertical(0.1F),
        ) {
            val background =
                if (isShowingBackgrounds) Color.White else Color.Transparent
            val shadowElevation =
                with(LocalDensity.current) { elevation.toDp() }
            val shape = RoundedCornerShape(15.dp)

            Card(
                shape = shape,
                backgroundColor = background,
                elevation = 0.dp,
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
                shape = shape,
                backgroundColor = background,
                elevation = 0.dp,
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