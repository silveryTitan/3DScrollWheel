package com.silverytitan.scrollwheel3d.view

import android.content.Context
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageView

class RotationImageView(context: Context, @Nullable attrs: AttributeSet) :
    AppCompatImageView(context, attrs) {
    private lateinit var paint: Paint
    private lateinit var shader: BitmapShader

    init {
        initFunction()
    }

    private fun initFunction() {
        paint = Paint()
        if (drawable is BitmapDrawable) {
            val bitmap = (drawable as BitmapDrawable).bitmap
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.isAntiAlias = true
        paint.isDither = true
        if (this::shader.isInitialized) {
            paint.shader = shader
        }
        canvas!!.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)
    }
}