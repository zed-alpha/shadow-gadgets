package com.zedalpha.shadowgadgets.demo.topic

import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.SeekChangeListener
import com.zedalpha.shadowgadgets.demo.ZedAlphaControl
import com.zedalpha.shadowgadgets.drawable.ShadowDrawable


class DrawablesFragment : TopicFragment(R.layout.fragment_drawables) {
    override val targetIds = intArrayOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exampleView = view.findViewById<View>(R.id.view_drawables)
        val shadowDrawable = ShadowDrawable.fromPath(DemoPath)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        shadowDrawable.fillPaint = paint
        exampleView.background = shadowDrawable

        val zac = view.findViewById<ZedAlphaControl>(R.id.zac_drawables)
        zac.listener =
            object : ZedAlphaControl.Listener {
                override fun onElevationChange(elevation: Float) {
                    shadowDrawable.elevation = elevation
                }

                override fun onColorChange(color: Int) {
                    paint.color = color
                    shadowDrawable.invalidateSelf()
                }
            }

        val scale = view.findViewById<SeekBar>(R.id.seek_scale)
        scale.setOnSeekBarChangeListener(
            object : SeekChangeListener() {
                override fun onChange(progress: Int) {
                    val value = 2F * progress / 100
                    shadowDrawable.scaleX = value
                    shadowDrawable.scaleY = value
                }
            }
        )
        val rotation = view.findViewById<SeekBar>(R.id.seek_rotation_z)
        rotation.setOnSeekBarChangeListener(
            object : SeekChangeListener() {
                override fun onChange(progress: Int) {
                    shadowDrawable.rotationZ = 360F * progress / 100 - 180F
                }
            }
        )
    }
}

private val TeardropPath =
    Path().also { teardrop ->
        teardrop.addRoundRect(
            0F,
            0F,
            200F,
            200F,
            floatArrayOf(100F, 100F, 0F, 0F, 100F, 100F, 100F, 100F),
            Path.Direction.CW
        )
    }

private val PuzzlePath =
    Path().also { puzzle ->
        // top
        puzzle.moveTo(0F, 50F)
        puzzle.lineTo(50F, 50F)
        puzzle.arcTo(50F, 0F, 100F, 50F, 100F, 340F, false)
        puzzle.lineTo(100F, 50F)
        puzzle.lineTo(150F, 50F)

        // right
        puzzle.lineTo(150F, 100F)
        puzzle.arcTo(150F, 100F, 200F, 150F, 190F, 340F, false)
        puzzle.lineTo(150F, 150F)
        puzzle.lineTo(150F, 200F)

        // bottom
        puzzle.lineTo(100F, 200F)
        puzzle.arcTo(50F, 150F, 100F, 200F, 80F, -340F, false)
        puzzle.lineTo(50F, 200F)
        puzzle.lineTo(0F, 200F)

        // left
        puzzle.lineTo(0F, 150F)
        puzzle.arcTo(0F, 100F, 50F, 150F, 170F, -340F, false)
        puzzle.lineTo(0F, 100F)
        puzzle.lineTo(0F, 50F)

        puzzle.close()
    }

private val DemoPath =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) PuzzlePath else TeardropPath