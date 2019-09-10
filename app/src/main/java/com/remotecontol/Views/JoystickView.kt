package com.remotecontol.Views

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


class JoystickView : SurfaceView, SurfaceHolder.Callback, View.OnTouchListener {
    var centerX = 0.0f
    var centerY = 0.0f
    var baseR = 0.0f
    var hatR = 0.0f

    lateinit var joystickCallback: JoystickListener

    constructor(context: Context) : super(context) {
        setZOrderOnTop(true)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSPARENT)
        setOnTouchListener(this)

        if (context is JoystickListener)
            joystickCallback = context
    }

    constructor(context: Context, attributes: AttributeSet, styles: Int) : super(
        context,
        attributes,
        styles
    ) {
        setZOrderOnTop(true)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSPARENT)
        setOnTouchListener(this)

        if (context is JoystickListener)
            joystickCallback = context
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        setZOrderOnTop(true)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSPARENT)
        setOnTouchListener(this)

        if (context is JoystickListener)
            joystickCallback = context
    }


    override fun surfaceCreated(p0: SurfaceHolder?) {
        setupDimensions()
        drawJoystick(centerX, centerY)
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    private fun drawJoystick(newX: Float, newY: Float) {
        if (holder.surface.isValid) {
            val canvas = holder.lockCanvas()
            val colors = Paint()
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            colors.setARGB(125, 50, 50, 50)  // set base color
            canvas.drawCircle(centerX, centerY, baseR, colors) // draw base

            colors.setARGB(255, 0, 100, 200) // set joystick color
            canvas.drawCircle(newX, newY, hatR, colors) // draw joystick

            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun setupDimensions() {
        centerX = width / 2f
        centerY = height / 2f

        baseR = min(width, height) / 4f
        hatR = min(width, height) / 7f

    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

        if (view.equals(this)) {
            if (motionEvent.action != MotionEvent.ACTION_UP) {

                val displacement =
                    sqrt((motionEvent.x - centerX).pow(2) + (motionEvent.y - centerY).pow(2))

                if (displacement < baseR) {
                    drawJoystick(motionEvent.x, motionEvent.y)
                    joystickCallback.onJoystickMoved(
                        (motionEvent.x - centerX) / baseR,
                        (motionEvent.y - centerY) / baseR,
                        id
                    )
                } else {
                    val ratio = baseR / displacement
                    val constrainedX = centerX + (motionEvent.x - centerX) * ratio
                    val constrainedY = centerY + (motionEvent.y - centerY) * ratio

                    drawJoystick(constrainedX, constrainedY)
                    joystickCallback.onJoystickMoved(
                        (constrainedX - centerX) / baseR,
                        (constrainedY - centerY) / baseR,
                        id
                    )
                }
            } else {
                drawJoystick(centerX, centerY)
                joystickCallback.onJoystickMoved(0.0f, 0.0f, id)
            }
        }

        return true
    }

    interface JoystickListener {

        fun onJoystickMoved(xPercent: Float, yPercent: Float, source: Int)
    }
}