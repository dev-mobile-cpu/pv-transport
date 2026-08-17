package com.pv.transport.ui.theme

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt

/**
 * In-app toast: white card, dark gray text. Same look for every save/error message.
 */
object AppToast {
    fun show(context: Context, message: String) {
        val density = context.resources.displayMetrics.density
        val text = TextView(context).apply {
            this.text = message
            setTextColor("#212529".toColorInt())
            textSize = 14f
            setPadding(
                (16 * density).toInt(),
                (12 * density).toInt(),
                (16 * density).toInt(),
                (12 * density).toInt()
            )
            background = GradientDrawable().apply {
                setColor("#FFFFFFFF".toColorInt())
                cornerRadius = 12 * density
                setStroke((1 * density).toInt(), "#E8E8E8".toColorInt())
            }
            elevation = 4 * density
        }
        Toast(context.applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, (72 * density).toInt())
            view = text
            show()
        }
    }
}
