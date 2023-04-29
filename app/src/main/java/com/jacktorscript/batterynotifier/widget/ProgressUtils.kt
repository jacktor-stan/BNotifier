package com.jacktorscript.batterynotifier.widget

import android.content.res.Resources


object ProgressUtils {
    fun dp2px(resources: Resources, dp: Int): Float {
        val scale = resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    fun sp2px(resources: Resources, sp: Int): Float {
        val scale = resources.displayMetrics.scaledDensity
        return sp * scale
    }

    /*fun getBitmap(context: Context?, drawableId: Int): Bitmap {
        val drawable = AppCompatResources.getDrawable(context!!, drawableId)
        return getBitmap(drawable)
    }

    private fun getBitmap(drawable: Drawable?): Bitmap {
        return when (drawable) {
            is BitmapDrawable -> {
                drawable.bitmap
            }
            is VectorDrawableCompat, is VectorDrawable -> {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
            else -> {
                throw IllegalArgumentException("unsupported drawable type")
            }
        }
    }

     */
}