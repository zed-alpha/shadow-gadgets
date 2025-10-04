package com.zedalpha.shadowgadgets.view.shadow

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.withSave
import com.zedalpha.shadowgadgets.view.internal.ThreadLocalGraphicsTemps
import com.zedalpha.shadowgadgets.view.internal.clipOutPath
import com.zedalpha.shadowgadgets.view.internal.getOutlineRadius
import com.zedalpha.shadowgadgets.view.internal.getOutlineRect
import java.lang.reflect.Field

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

        val bounds = temps.rect
        when {
            getOutlineRect(outline, bounds) && !bounds.isEmpty -> {
                val radius = getOutlineRadius(outline)
                val boundsF = temps.rectF
                boundsF.set(bounds)
                clip.addRoundRect(boundsF, radius, radius, Path.Direction.CW)
            }

            Build.VERSION.SDK_INT < 30 -> {
                OutlinePathReflector.getPath(outline, clip)
            }

            else -> pathProvider?.getPath(clip)
        }
    }

    override fun draw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated || clip.isEmpty) return

        val shadow = this.shadow
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

private object OutlinePathReflector {

    @SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
    private val mPath: Field? =
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                Class::class.java
                    .getDeclaredMethod("getDeclaredField", String::class.java)
                    .invoke(Outline::class.java, "mPath") as Field
            } else {
                Outline::class.java.getDeclaredField("mPath")
            }
        } catch (_: Exception) {
            null
        }

    fun getPath(outline: Outline, path: Path): Boolean {
        val pathField = mPath ?: return false
        val outlinePath =
            try {
                pathField.get(outline) as Path
            } catch (_: Exception) {
                null
            }
        return outlinePath?.let { path.set(it); true } ?: false
    }
}