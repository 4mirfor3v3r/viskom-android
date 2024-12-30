package amirlabs.plate_detection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View


class BoundingBoxView : View {
    private var rect: Rect? = null
    private var paint: Paint? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        paint = Paint()
        paint!!.color = Color.RED
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = 5f
    }

    fun setBoundingBox(l: Int, t: Int, r: Int, b: Int) {
        this.rect = Rect(l, t, r, b)
        invalidate() // Meminta sistem menggambar ulang
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rect != null) {
            canvas.drawRect(rect!!, paint!!)
        }
    }
}