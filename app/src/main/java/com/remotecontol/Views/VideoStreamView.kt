package com.remotecontol.Views

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.widget.VideoView
import kotlinx.android.synthetic.main.activity_fullscreen.view.*


class VideoStreamView : VideoView, SurfaceHolder.Callback {
    constructor(context: Context) : super(context) {
        holder.addCallback(this)
    }

    constructor(context: Context, attrbs: AttributeSet) : super(context, attrbs) {
        holder.addCallback(this)
    }

    constructor(context: Context, attrbs: AttributeSet, style: Int) : super(
        context,
        attrbs,
        style
    ) {
        holder.addCallback(this)
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        //drawBackground()
        videoView.setBackgroundColor(Color.RED)
        videoView.start()
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    fun drawBackground() {
        if (holder.surface.isValid) {
            val canvas = holder.lockCanvas()
            val textcolor = Paint()
            textcolor.textSize = 24f

            canvas.drawColor(Color.CYAN)
            canvas.drawText("No Video Avaiable", canvas.width / 2f, canvas.height / 2f, textcolor)

            videoView.draw(canvas)
            holder.unlockCanvasAndPost(canvas)
        }
    }
}