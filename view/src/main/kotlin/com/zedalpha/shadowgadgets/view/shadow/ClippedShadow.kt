package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.withSave
import com.zedalpha.shadowgadgets.view.internal.OutlinePathReflector
import com.zedalpha.shadowgadgets.view.internal.ThreadLocalGraphicsTemps
import com.zedalpha.shadowgadgets.view.internal.clipOutPath
import com.zedalpha.shadowgadgets.view.internal.getOutlineRadius
import com.zedalpha.shadowgadgets.view.internal.getOutlineRect

internal fun interface PathProvider {
    fun getPath(path: Path)
}

internal class ClippedShadow(val shadow: Shadow) : Shadow by shadow {

    constructor(link: View, forceViewType: Boolean = false) :
            this(Shadow(link, forceViewType))

    @RequiresApi(29)
    constructor() : this(Shadow())

    private val clip = Path()

    private val temps = ThreadLocalGraphicsTemps

    var pathProvider: PathProvider? = null

    override fun setOutline(outline: Outline) {
        shadow.setOutline(outline)

        val clip = this.clip
        clip.rewind()

        if (outline.isEmpty) return

        val radius = getOutlineRadius(outline)
        when {
            radius >= 0F -> {
                val rect = temps.rect

                getOutlineRect(outline, rect)
                if (rect.isEmpty) return

                // No special case for zero radius. Seems Skia may skip the
                // shadow draw if a rect clip-out matches the content area.
                clip.addRoundRect(
                    /* left = */ rect.left.toFloat(),
                    /* top = */ rect.top.toFloat(),
                    /* right = */ rect.right.toFloat(),
                    /* bottom = */ rect.bottom.toFloat(),
                    /* rx = */ radius,
                    /* ry = */ radius,
                    /* dir = */ Path.Direction.CW
                )
            }

            Build.VERSION.SDK_INT < 30 -> {
                OutlinePathReflector.getPath(outline, clip)
            }

            else -> pathProvider?.getPath(clip)
        }
    }

    override fun draw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated) return

        val clip = this.clip
        if (clip.isEmpty) return

        val shadow = this.shadow
        val temps = this.temps
        val matrix = temps.matrix
        val path = temps.path

        getMatrix(matrix)
        matrix.postTranslate(shadow.left.toFloat(), shadow.top.toFloat())
        clip.transform(matrix, path)

        canvas.withSave {
            clipOutPath(this, path)
            shadow.draw(this)
        }
    }
}