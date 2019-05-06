package com.pixelart.sceneformarcoreexample

import android.graphics.*
import android.graphics.drawable.Drawable

class PointerDrawable: Drawable() {
    private val paint = Paint()
    private var enabled: Boolean = false

    override fun draw(canvas: Canvas) {
        val canvasX: Float = (canvas.width / 2).toFloat()
        val canvasY: Float = (canvas.height / 2).toFloat()

        if(enabled){
            paint.color = Color.GREEN
            canvas.drawCircle(canvasX, canvasY, 10F, paint)
        }else{
            paint.color = Color.GRAY
            canvas.drawText("X", canvasX, canvasY, paint)
        }
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    fun setEnabled(enabled: Boolean){
        this.enabled = enabled
    }

    fun getEnabled(): Boolean = enabled
}