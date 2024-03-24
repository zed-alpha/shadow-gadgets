package com.zedalpha.shadowgadgets.compose.internal

import android.os.Build
import android.view.View
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSimple
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.PathProvider
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.layer.Layer
import kotlin.math.roundToInt
import android.graphics.Outline as AndroidOutline
import android.graphics.Path as AndroidPath

internal class ComposeShadow(
    private val view: View,
    clipped: Boolean
) {
    private val androidPath = AndroidPath()

    private val androidOutline = AndroidOutline().apply { alpha = 1.0F }

    private val coreShadow = when {
        clipped -> ClippedShadow(view).apply {
            pathProvider = PathProvider { it.set(androidPath) }
        }

        else -> Shadow(view)
    }

    private var layer: Layer? = null

    private var layerOffset = InitialOffset
        set(value) {
            if (field == value) return
            field = value
            layer?.recreate()
        }

    private val layoutListener =
        View.OnLayoutChangeListener { _, l, t, r, b, _, _, _, _ ->
            layer?.setSize(r - l, b - t)
        }

    init {
        view.addOnLayoutChangeListener(layoutListener)
    }

    fun dispose() {
        view.removeOnLayoutChangeListener(layoutListener)
        coreShadow.dispose()
        layer?.dispose()
    }

    fun prepare(
        bounds: Rect,
        ambientColor: Color,
        spotColor: Color,
        colorCompat: Color
    ) {
        coreShadow.setPosition(
            bounds.left.roundToInt(),
            bounds.top.roundToInt(),
            bounds.right.roundToInt(),
            bounds.bottom.roundToInt()
        )
        if (requiresLayer(colorCompat)) {
            val layer = layer?.apply { refresh() } ?: createLayer(view)
            layer.color = colorCompat.toArgb()
            coreShadow.ambientColor = DefaultShadowColorInt
            coreShadow.spotColor = DefaultShadowColorInt
        } else {
            layer?.run { dispose(); layer = null }
            coreShadow.ambientColor = ambientColor.toArgb()
            coreShadow.spotColor = spotColor.toArgb()
        }
    }

    private fun createLayer(view: View) =
        Layer(
            view,
            DefaultShadowColorInt,
            view.width,
            view.height
        ).also { newLayer ->
            newLayer.addDraw(coreShadow::draw)
            layer = newLayer
        }

    fun draw(
        scope: DrawScope,
        elevation: Dp,
        shape: Shape,
        offsetInRoot: Offset,
        layerOffset: IntOffset
    ) {
        val androidCanvas = scope.drawContext.canvas.nativeCanvas
        if (!androidCanvas.isHardwareAccelerated) return

        coreShadow.elevation = with(scope) { elevation.toPx() }
        applyShape(shape, scope.size, scope.layoutDirection, scope)
        this.layerOffset = layerOffset

        androidCanvas.translate(-offsetInRoot.x, -offsetInRoot.y)
        layer?.draw(androidCanvas) ?: coreShadow.draw(androidCanvas)
        androidCanvas.translate(offsetInRoot.x, offsetInRoot.y)
    }

    private var shape: Shape = RectangleShape
    private var size: Size = Size.Zero
    private var layoutDirection = LayoutDirection.Ltr
    private var density = Density(0F, 0F)

    private fun applyShape(
        shape: Shape,
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) {
        var unchanged = true
        if (this.shape != shape) {
            this.shape = shape
            unchanged = false
        }
        if (this.size != size) {
            this.size = size
            unchanged = false
        }
        if (this.layoutDirection != layoutDirection) {
            this.layoutDirection = layoutDirection
            unchanged = false
        }
        if (this.density != density) {
            // The passed density ia a DrawScope. We don't want to hold onto it.
            this.density = Density(density.density, density.fontScale)
            unchanged = false
        }
        if (unchanged) return

        shape.createOutline(size, layoutDirection, density)
            .applyTo(androidOutline, androidPath)

        coreShadow.setOutline(androidOutline)
    }
}

private fun Outline.applyTo(
    androidOutline: AndroidOutline,
    androidPath: AndroidPath
) {
    androidPath.reset()
    when (this) {
        is Outline.Rectangle -> setRect(rect, androidOutline, androidPath)
        is Outline.Rounded -> setRound(roundRect, androidOutline, androidPath)
        is Outline.Generic -> setPath(path, androidOutline, androidPath)
    }
}

private fun setRect(
    rect: Rect,
    androidOutline: AndroidOutline,
    androidPath: AndroidPath
) {
    androidOutline.setRect(
        rect.left.roundToInt(),
        rect.top.roundToInt(),
        rect.right.roundToInt(),
        rect.bottom.roundToInt()
    )
    androidPath.addRect(
        rect.left,
        rect.top,
        rect.right,
        rect.bottom,
        AndroidPath.Direction.CW
    )
}

private var tmpComposePath: Path? = null

private fun setRound(
    roundRect: RoundRect,
    androidOutline: AndroidOutline,
    androidPath: AndroidPath
) {
    val radius = roundRect.topLeftCornerRadius.x
    if (roundRect.isSimple) {
        androidOutline.setRoundRect(
            roundRect.left.roundToInt(),
            roundRect.top.roundToInt(),
            roundRect.right.roundToInt(),
            roundRect.bottom.roundToInt(),
            radius
        )
        androidPath.addRoundRect(
            roundRect.left,
            roundRect.top,
            roundRect.right,
            roundRect.bottom,
            radius,
            radius,
            AndroidPath.Direction.CW
        )
    } else {
        val path = tmpComposePath ?: Path().also { tmpComposePath = it }
        setPath(
            path.apply { reset(); addRoundRect(roundRect) },
            androidOutline,
            androidPath
        )
    }
}

private fun setPath(
    composePath: Path,
    androidOutline: AndroidOutline,
    androidPath: AndroidPath
) {
    if (composePath.isEmpty) return

    val path = composePath.asAndroidPath()
    when {
        Build.VERSION.SDK_INT >= 30 -> {
            OutlinePathHelper.setPath(androidOutline, path)
        }

        Build.VERSION.SDK_INT >= 29 -> try {  // Early Q fumbled this
            @Suppress("DEPRECATION")
            androidOutline.setConvexPath(path)
        } catch (e: IllegalArgumentException) {
            return  // Leave androidPath empty
        }

        else -> {
            @Suppress("DEPRECATION")
            if (path.isConvex) androidOutline.setConvexPath(path)
        }
    }
    androidPath.set(path)
}

@RequiresApi(30)
private object OutlinePathHelper {

    @DoNotInline
    fun setPath(outline: AndroidOutline, path: AndroidPath) {
        outline.setPath(path)
    }
}