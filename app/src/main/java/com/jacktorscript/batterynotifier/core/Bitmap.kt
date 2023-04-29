package com.jacktorscript.batterynotifier.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

object Bitmap {
    fun textAsBitmap(text: String?, textSize: Float, textColor: Int): Bitmap? {
        val paint = Paint()
        paint.color = textColor
        paint.textSize = textSize
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.LEFT
        //val baseline = -paint.ascent() // ascent() is negative

        val baseline = if (text!!.toInt() >= 100) {
            120f
        } else {
            -paint.ascent()
        }

        var width = (paint.measureText(text) + 0.0f).toInt() // round
        var height = (baseline + paint.descent() + 0.0f).toInt()
        val trueWidth = width
        if (width > height) height = width else width = height
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text, (width / 2 - trueWidth / 2).toFloat(), baseline, paint)
        return image
    }
}