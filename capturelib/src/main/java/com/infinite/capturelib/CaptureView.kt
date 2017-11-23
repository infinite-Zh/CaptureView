package com.infinite.capturelib

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * Created by kfzhangxu on 2017/11/22.
 */
class CaptureView : View {

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
        private val TAG: String = CaptureView::class.java.name
        // corner边长
        private val CORNER_BORDER_LENGTH: Int = 40
        //corner 边宽度
        private val CORNER_BORDER_WIDTH: Int = 3

        private val CORNER_COLOR: Long = 0xff1296db
        private val OVERLAY_COLOR: Long = 0x5f000000
        private val FRAME_SIZE: Int = 400
        private val FRAME_MIN_SIZE = 50
    }

    var mCornerBorderLenght = CORNER_BORDER_LENGTH
    var mCornerBorderWidth = CORNER_BORDER_WIDTH
    // 偏移范围
    val TOUCH_SLOP = 50f
    var mTouchMode: Mode? = null
    var mZoomMode: ZOOM_POINT? = null
    var mOutterWidth: Int = 600
    var mOutterHeight: Int = 600
    var mInnerWidth: Int = FRAME_SIZE
    var mInnerHeight: Int = FRAME_SIZE

    var mOutterPaint: Paint? = null
    var mOverlayColor = OVERLAY_COLOR

    var mInnerPaint: Paint? = null
    var mInnerBackground = 0xFF5151
    var mOutterRect: Rect? = null
    var mFrameRect: RectF? = null

    var mCornerPaint: Paint? = null
    var mCornerColor = CORNER_COLOR
    var mCornerWidth = 5f
    var mFrameMinSize = FRAME_MIN_SIZE

    constructor(context: Context) : super(context) {
        initResource()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        getAttrs(context, attributeSet, 0)
        initResource()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        getAttrs(context, attributeSet, defStyleAttr)
        initResource()
    }

    private fun getAttrs(context: Context, attr: AttributeSet, defStyleAttr: Int) {
        val ta: TypedArray = context.obtainStyledAttributes(attr, R.styleable.CaptureView, defStyleAttr, 0)
        mCornerBorderLenght = ta.getDimensionPixelSize(R.styleable.CaptureView_frame_corner_border_length, CORNER_BORDER_LENGTH)
        mCornerBorderWidth = ta.getDimensionPixelSize(R.styleable.CaptureView_frame_corner_border_width, CORNER_BORDER_WIDTH)
        mCornerColor = ta.getColor(R.styleable.CaptureView_frame_corner_color, CORNER_COLOR.toInt()).toLong()
        mOverlayColor = ta.getColor(R.styleable.CaptureView_overlay_color, OVERLAY_COLOR.toInt()).toLong()
        mInnerHeight = ta.getDimensionPixelSize(R.styleable.CaptureView_frame_default_size, FRAME_SIZE)
        mFrameMinSize = ta.getDimensionPixelSize(R.styleable.CaptureView_frame_min_size, FRAME_MIN_SIZE)
        mInnerWidth = mInnerHeight
        ta.recycle()
    }

    private fun initResource() {
        mOutterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mOutterPaint!!.color = mOverlayColor.toInt()
        mOutterPaint!!.style = Paint.Style.FILL

        mInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mInnerPaint!!.color = mInnerBackground
        mInnerPaint!!.style = Paint.Style.FILL_AND_STROKE
        mInnerPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        mCornerPaint = Paint()
        mCornerPaint!!.color = mCornerColor.toInt()
        mCornerPaint!!.strokeWidth = mCornerWidth
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
            Log.e(TAG, "dx=$mDx,dy=$mDy")
            when (mZoomMode) {
            //左侧拉伸
                ZOOM_POINT.LEFT -> {
                    if (checkFrameWidthSize() && mDx > 0) {
                    } else {
                        mFrameRect!!.left += mDx

                    }
                }
            // stretch top
                ZOOM_POINT.TOP -> {
                    if (checkFrameHeightSize() && mDy > 0) {
                    } else {
                        mFrameRect!!.top += mDy
                    }
                }
            //stretch right
                ZOOM_POINT.RIGHT -> {
                    if (checkFrameWidthSize() && mDx < 0) {
                    } else {
                        mFrameRect!!.right += mDx
                    }
                }
            //stretch right
                ZOOM_POINT.BOTTOM -> {
                    if (checkFrameHeightSize() && mDy < 0) {
                    } else {
                        mFrameRect!!.bottom += mDy
                    }
                }
            //stretch left_top
                ZOOM_POINT.LEFT_TOP -> {
                    if (checkFrameWidthSize() && mDx < 0) {
                    } else {
                        mFrameRect!!.left += mDx

                    }
                    if (checkFrameHeightSize() && mDy > 0) {
                    } else {
                        mFrameRect!!.top += mDy
                    }
                }
                ZOOM_POINT.RIGHT_TOP -> {
                    if (checkFrameHeightSize() && mDy > 0) {
                    } else {
                        mFrameRect!!.top += mDy

                    }
                    if (checkFrameWidthSize() && mDx < 0) {
                    } else {
                        mFrameRect!!.right += mDx

                    }
                }
                ZOOM_POINT.RIGHT_BOTTOM -> {
                    if (checkFrameWidthSize() && mDx < 0) {
                    } else {
                        mFrameRect!!.right += mDx
                    }
                    if (checkFrameHeightSize() && mDy < 0) {
                    } else {
                        mFrameRect!!.bottom += mDy
                    }
                }
                ZOOM_POINT.LEFT_BOTTOM -> {
                    if (checkFrameWidthSize() && mDx > 0) {
                    } else {
                        mFrameRect!!.left += mDx
                    }
                    if (checkFrameHeightSize() && mDy < 0) {
                    } else {
                        mFrameRect!!.bottom += mDy
                    }
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

    private fun checkFrameWidthSize(): Boolean {
        // the width / height of the frame is smaller than FRAME_MIN_SIZE
        if (mFrameRect!!.width() <= mFrameMinSize) {
            return true
        }
        return false
    }

    private fun checkFrameHeightSize(): Boolean {
        // the width / height of the frame is smaller than FRAME_MIN_SIZE
        if (mFrameRect!!.height() <= mFrameMinSize) {
            return true
        }
        return false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawOverlay(canvas)
        drawCorner(canvas)
    }

    private fun drawOverlay(canvas: Canvas?) {
        canvas!!.drawRect(mOutterRect, mOutterPaint)
        canvas.drawRect(mFrameRect, mInnerPaint)
    }


    var mPath: Path = Path()
    private fun drawCorner(canvas: Canvas?) {
        val l = mFrameRect!!.left + 5
        val t = mFrameRect!!.top + 5
        val r = mFrameRect!!.right - 5
        val b = mFrameRect!!.bottom - 5
        // 左上角
        canvas!!.drawRect(l, t, l + mCornerBorderWidth, t + mCornerBorderLenght, mCornerPaint)
        canvas!!.drawRect(l, t, l + mCornerBorderLenght, t + mCornerBorderWidth, mCornerPaint)

        //右上角
        canvas.drawRect(r - mCornerBorderLenght, t, r, t + mCornerBorderWidth, mCornerPaint)
        canvas.drawRect(r - mCornerBorderWidth, t, r, t + mCornerBorderLenght, mCornerPaint)

        //右下角
        canvas.drawRect(r - mCornerBorderWidth, b - mCornerBorderLenght, r, b, mCornerPaint)
        canvas.drawRect(r - mCornerBorderLenght, b - mCornerBorderWidth, r, b, mCornerPaint)

        //左下角
        canvas.drawRect(l, b - mCornerBorderLenght, l + mCornerBorderWidth, b, mCornerPaint)
        canvas.drawRect(l, b - mCornerBorderWidth, l + mCornerBorderLenght, b, mCornerPaint)


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