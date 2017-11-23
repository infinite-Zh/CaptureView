package com.infinite.demo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * Created by kfzhangxu on 2017/11/22.
 */
class ScreenShowView : View {

    enum class Mode {
        Mode_Translate, Mode_Zoom
    }

    enum class ZOOM_POINT {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,
        LEFT_TOP,
        RIGHT_TOP,
        RIGHT_BOTTOM,
        LEFT_BOTTOM
    }

    companion object {
        private val TAG: String = ScreenShowView::class.java.name
        // corner边长
        private val CORNER_BORDER_LENGTH: Float = 40f
        //corner 边宽度
        private val CORNER_BORDER_WIDTH: Float = 3f
    }

    // 偏移范围
    val TOUCH_SLOP = 50f
    var mTouchMode: Mode? = null
    var mZoomMode: ZOOM_POINT? = null
    var mOutterWidth: Int = 600
    var mOutterHeight: Int = 600
    var mInnerWidth: Int = 400
    var mInnerHeight: Int = 400

    var mOutterPaint: Paint? = null
    var mInnerPaint: Paint? = null
    var mOutterRect: Rect? = null
    var mFrameRect: RectF? = null

    var mCornerPaint: Paint? = null
    var mCornerColor = 0xff1296db
    var mCornerWidth = 5f

    constructor(context: Context) : super(context) {
        initResource()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initResource()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        initResource()
    }

    private fun initResource() {
        mOutterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mOutterPaint!!.color = 0x5f000000
        mOutterPaint!!.style = Paint.Style.FILL

        mInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mInnerPaint!!.color = 0xFF5151
        mInnerPaint!!.style = Paint.Style.FILL_AND_STROKE
        mInnerPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        mCornerPaint = Paint()
        mCornerPaint!!.color = mCornerColor.toInt()
        mCornerPaint!!.strokeWidth = mCornerWidth
        mCornerPaint!!.textSize = 50f
        mCornerPaint!!.style = Paint.Style.FILL_AND_STROKE
        setLayerType(LAYER_TYPE_HARDWARE, null)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.AT_MOST -> widthSize = 100
            MeasureSpec.UNSPECIFIED -> widthSize = 100
            MeasureSpec.EXACTLY -> {
            }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST -> heightSize = 500
            MeasureSpec.UNSPECIFIED -> heightSize = 500
            MeasureSpec.EXACTLY -> {
            }
        }
        mOutterWidth = widthSize
        mOutterHeight = heightSize
        setMeasuredDimension(widthSize, heightSize)
        Log.e(TAG, "width=$mOutterWidth,height=$mOutterHeight")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mOutterHeight = h
        mOutterWidth = w
        mOutterRect = Rect(0, 0, mOutterWidth, mOutterHeight)

        mFrameRect = RectF(
                ((mOutterWidth - mInnerWidth) / 2).toFloat(),
                ((mOutterHeight - mInnerHeight) / 2).toFloat(),
                ((mOutterWidth + mInnerWidth) / 2).toFloat(),
                ((mOutterHeight + mInnerHeight) / 2).toFloat())

        invalidate()
    }

    private fun updateFrameRect() {
        if (mTouchMode == Mode.Mode_Translate) {

            mFrameRect!!.top = mFrameRect!!.top + mDy
            mFrameRect!!.bottom = mFrameRect!!.bottom + mDy

            mFrameRect!!.right = mFrameRect!!.right + mDx
            mFrameRect!!.left = mFrameRect!!.left + mDx
            //横向超出边界
            if (mFrameRect!!.left < 0 || mFrameRect!!.right > mOutterWidth) {
                if (mFrameRect!!.left < 0) {
                    mFrameRect!!.left = 0f
                    mFrameRect!!.right = mInnerWidth.toFloat()
                } else if (mFrameRect!!.right > mOutterWidth) {
                    mFrameRect!!.left = (mOutterWidth - mInnerWidth).toFloat()
                    mFrameRect!!.right = mOutterWidth.toFloat()
                }
            }
            // 纵向超出边界
            if (mFrameRect!!.top < 0 || mFrameRect!!.bottom > mOutterHeight) {
                if (mFrameRect!!.top < 0) {
                    mFrameRect!!.top = 0f
                    mFrameRect!!.bottom = mInnerHeight.toFloat()
                } else if (mFrameRect!!.bottom > mOutterHeight) {
                    mFrameRect!!.top = (mOutterHeight - mInnerHeight).toFloat()
                    mFrameRect!!.bottom = mOutterHeight.toFloat()
                }
            }
        } else if (mTouchMode == Mode.Mode_Zoom) {
            when (mZoomMode) {
            //左侧拉伸
                ZOOM_POINT.LEFT -> {
                    mFrameRect!!.left += mDx
                }
            // stretch top
                ZOOM_POINT.TOP -> {
                    mFrameRect!!.top += mDy
                }
            //stretch right
                ZOOM_POINT.RIGHT -> {
                    mFrameRect!!.right += mDx
                }
            //stretch right
                ZOOM_POINT.BOTTOM -> {
                    mFrameRect!!.bottom += mDy
                }
            //stretch left_top
                ZOOM_POINT.LEFT_TOP -> {
                    mFrameRect!!.left += mDx
                    mFrameRect!!.top += mDy
                }
                ZOOM_POINT.RIGHT_TOP -> {
                    mFrameRect!!.top += mDy
                    mFrameRect!!.right += mDx
                }
                ZOOM_POINT.RIGHT_BOTTOM -> {
                    mFrameRect!!.right += mDx
                    mFrameRect!!.bottom += mDy
                }
                ZOOM_POINT.LEFT_BOTTOM -> {
                    mFrameRect!!.left += mDx
                    mFrameRect!!.bottom += mDy
                }
            }
        }
        if (mFrameRect!!.left < 0) mFrameRect!!.left = 0f
        if (mFrameRect!!.right > mOutterWidth) mFrameRect!!.right = mOutterWidth.toFloat()
        if (mFrameRect!!.top < 0) mFrameRect!!.top = 0f
        if (mFrameRect!!.bottom > mOutterHeight) mFrameRect!!.bottom = mOutterHeight.toFloat()
        mInnerWidth = (mFrameRect!!.right - mFrameRect!!.left).toInt()
        mInnerHeight = (mFrameRect!!.bottom - mFrameRect!!.top).toInt()
        invalidate()


    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawOverlay(canvas)
        drawCorner(canvas)
    }

    private fun drawOverlay(canvas: Canvas?) {
        canvas!!.save()
        canvas!!.drawRect(mOutterRect, mOutterPaint)
//        mInnerPaint!!.xfermode=PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRect(mFrameRect, mInnerPaint)
        canvas!!.restore()
    }


    var mPath: Path = Path()
    private fun drawCorner(canvas: Canvas?) {
//        mInnerPaint!!.xfermode=null

//        mPath.reset()
        val l = mFrameRect!!.left + 5
        val t = mFrameRect!!.top + 5
        val r = mFrameRect!!.right - 5
        val b = mFrameRect!!.bottom - 5
//        mPath.moveTo(l, t - CORNER_BORDER_LENGTH)
//        mPath.lineTo(l, t)
//        mPath.lineTo(l + CORNER_BORDER_LENGTH, t)
//        canvas!!.drawPath(mPath, mCornerPaint)
//        mPath.moveTo(r - CORNER_BORDER_LENGTH, t)
//        mPath.lineTo(r, t)
//        mPath.lineTo(r, t - CORNER_BORDER_LENGTH)
//        canvas.drawPath(mPath, mCornerPaint)
//
//        canvas.drawText("hhhhh", 0, 5, 200f, 200f, mCornerPaint)
        // 左上角
        canvas!!.drawRect(l, t, l + CORNER_BORDER_WIDTH, t + CORNER_BORDER_LENGTH, mCornerPaint)
        canvas!!.drawRect(l, t, l + CORNER_BORDER_LENGTH, t + CORNER_BORDER_WIDTH, mCornerPaint)

        //右上角
        canvas.drawRect(r - CORNER_BORDER_LENGTH, t, r, t + CORNER_BORDER_WIDTH, mCornerPaint)
        canvas.drawRect(r - CORNER_BORDER_WIDTH, t, r, t + CORNER_BORDER_LENGTH, mCornerPaint)

        //右下角
        canvas.drawRect(r - CORNER_BORDER_WIDTH, b - CORNER_BORDER_LENGTH, r, b, mCornerPaint)
        canvas.drawRect(r - CORNER_BORDER_LENGTH, b - CORNER_BORDER_WIDTH, r, b, mCornerPaint)

        //左下角
        canvas.drawRect(l, b - CORNER_BORDER_LENGTH, l + CORNER_BORDER_WIDTH, b, mCornerPaint)
        canvas.drawRect(l, b - CORNER_BORDER_WIDTH, l + CORNER_BORDER_LENGTH, b, mCornerPaint)


    }


    private var mDx: Float = 0f
    private var mDy: Float = 0f
    var curX = 0f
    var curY = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                curX = event.x
                curY = event.y
                mDx = 0f
                mDy = 0f
                //点击在frame框里面,并且距离各个边大于TOUCH_SLOP
                if (curX > mFrameRect!!.left + TOUCH_SLOP
                        && curX < mFrameRect!!.right - TOUCH_SLOP
                        && curY < mFrameRect!!.bottom - TOUCH_SLOP
                        && curY > mFrameRect!!.top + TOUCH_SLOP) {
                    mTouchMode = Mode.Mode_Translate
                } else {
                    setZoomMode(curX, curY)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                mDx = (event.x - curX)
                mDy = (event.y - curY)
                curX = event.x
                curY = event.y

                updateFrameRect()
                mDx = 0f
                mDy = 0f
            }
            MotionEvent.ACTION_UP -> {
                curX = 0f
                curY = 0f
                mDx = 0f
                mDy = 0f
                mTouchMode = null
                mZoomMode = null
            }
        }
        return true
    }

    // figure out the zoom mode
    private fun setZoomMode(x: Float, y: Float) {
        //左边界在有效缩放距离上
        var l = mFrameRect!!.left
        var t = mFrameRect!!.top
        var r = mFrameRect!!.right
        var b = mFrameRect!!.bottom
        //the left border
        if (getAbs(l, x) < TOUCH_SLOP && y <= b && y >= t) {
            mTouchMode = Mode.Mode_Zoom
            mZoomMode = ZOOM_POINT.LEFT
            //the top border
        } else if (getAbs(y, t) < TOUCH_SLOP && x >= l && x <= r) {
            mZoomMode = ZOOM_POINT.TOP
            mTouchMode = Mode.Mode_Zoom
            // the right border
        } else if (getAbs(x, r) < TOUCH_SLOP && y <= b && y > t) {
            mZoomMode = ZOOM_POINT.RIGHT
            mTouchMode = Mode.Mode_Zoom
            // the bottom border
        } else if (getAbs(y, b) < TOUCH_SLOP && x >= l && x <= r) {
            mZoomMode = ZOOM_POINT.BOTTOM
            mTouchMode = Mode.Mode_Zoom
        } else {
            mTouchMode = null
        }

        //left_top
        if (getPow((x - l).toDouble()) + getPow((y - t).toDouble()) <= getPow(TOUCH_SLOP.toDouble())) {
            mZoomMode = ZOOM_POINT.LEFT_TOP
        }

        //right_top
        if (getPow((x - r).toDouble()) + getPow((y - t).toDouble()) <= getPow(TOUCH_SLOP.toDouble())) {
            mZoomMode = ZOOM_POINT.RIGHT_TOP
        }
        //right_bottom
        if (getPow((x - r).toDouble()) + getPow((y - b).toDouble()) <= getPow(TOUCH_SLOP.toDouble())) {
            mZoomMode = ZOOM_POINT.RIGHT_BOTTOM
        }
        //right_top
        if (getPow((x - l).toDouble()) + getPow((y - b).toDouble()) <= getPow(TOUCH_SLOP.toDouble())) {
            mZoomMode = ZOOM_POINT.LEFT_BOTTOM
        }

        Log.e(TAG, "mode=$mZoomMode")
    }

    private fun getPow(i: Double): Double {
        return Math.pow(i, 2.0)
    }

    private fun getAbs(a: Float, b: Float): Float {
        return Math.abs(a - b)
    }

    fun shot(bitmap: Bitmap): Bitmap {
//        var ratioX: Float = bitmap.width * mFrameRect!!.width() / mOutterWidth
        var ratioX: Float = 1 / (mOutterWidth.toFloat() / bitmap.width.toFloat()).toFloat()
//        var ratioY: Float = bitmap.height * mFrameRect!!.height() / mOutterHeight
        var ratioY: Float = 1 / (mOutterHeight.toFloat() / bitmap.height.toFloat()).toFloat()

        var l: Int = (mFrameRect!!.left * ratioX).toInt()
        var t: Int = (mFrameRect!!.top * ratioY).toInt()
        var width: Int = (mFrameRect!!.width() * ratioX).toInt()
        var height: Int = (mFrameRect!!.height() * ratioY).toInt()
        //做一下边界控制,避免出现截图区域超出bitmap宽高的问题
        if (l < 0) l = 0
        if (t < 0) t = 0
        if (l + width > bitmap.width) width = bitmap.width - l
        if (t + height > bitmap.height) height = bitmap.height - t

        Log.e(TAG, "bWidth=${bitmap.width},bHeight=${bitmap.height}")
        Log.e(TAG, "ratiox=$ratioX,ratioY=$ratioY,l=$l,t=$t,width=$width,height=$height")
        val target: Bitmap = Bitmap.createBitmap(bitmap, l, t, width, height)
        return target
    }
}