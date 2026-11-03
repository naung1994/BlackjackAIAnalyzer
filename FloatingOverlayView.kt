package com.ai.blackjackanalyzer

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class FloatingOverlayView(private val context: Context) {

    private var windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private lateinit var txtPrediction: TextView
    private lateinit var txtRecommendation: TextView

    fun show() {
        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // ဤနေရာတွင် custom layout XML ဖိုင်ကို ချိတ်ဆက်နိုင်သည်
        txtPrediction = TextView(context).apply {
            text = "AI Analyzer: Waiting for live game..."
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#AA000000"))
            setPadding(20, 20, 20, 20)
        }

        overlayView = txtPrediction
        windowManager.addView(overlayView, params)
    }

    fun updateData(predictionText: String) {
        if (::txtPrediction.isInitialized) {
            txtPrediction.text = predictionText
        }
    }

    fun hide() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }
}
