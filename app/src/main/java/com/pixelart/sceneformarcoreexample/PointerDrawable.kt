package com.pixelart.sceneformarcoreexample

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable

class PointerDrawable(private val context: Context): Drawable() {
    private val paint = Paint()
    private var enabled: Boolean = false

    override fun draw(canvas: Canvas) {
        val canvasX: Float = (canvas.width / 2).toFloat()
        val canvasY: Float = (canvas.height / 2).toFloat()
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.marker_detector2 )

        if(enabled){
            paint.color = Color.GREEN
            val dx = (canvas.width - bitmap.width)/ 2
            val dy = (canvas.height - bitmap.height) / 2
            canvas.drawBitmap(bitmap, dx.toFloat(), dy.toFloat(), null)
            //canvas.drawCircle(canvasX, canvasY, 10F, paint)
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