package com.wzx.relationshiplib

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.collections.ArrayList


/**
 * 描述：
 *
 * 创建人： Administrator
 * 创建时间： 2018/7/24
 * 更新时间：
 * 更新内容：
 */
class RelationshipView : View {

    var x: Int = 0
    var y: Int = 0
    var mwidth: Int = 0
    var mheight: Int = 0

    var middleCirclePaint: Paint
    var otherCircleOuterPaint: Paint
    var otherCircleInnerPaint: Paint
    var linePaint: Paint
    var centerWordsPaint: Paint
    var otherWordsPaint: Paint
    var textRect: Rect
    var animatorValue = 0f
    var isReverse = false

    var paths: ArrayList<ShipPath> = ArrayList()

    var pathMeasures: ArrayList<PathMeasure> = ArrayList()

    var radiusArray: ArrayList<Int> = ArrayList()

    var stories: ArrayList<Ship> = ArrayList()
    var centerStory: Ship = Ship()


    private lateinit var onClickStoryListener: OnShipClickListener

    var clickItem = -1

    var count = 0

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        middleCirclePaint = Paint()
        middleCirclePaint.isAntiAlias = true
        middleCirclePaint.color = Color.parseColor("#F8D46F")
        linePaint = Paint()
        linePaint.isAntiAlias = true
        linePaint.color = Color.parseColor("#C5B170")
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = dip2px(1f)
        otherCircleOuterPaint = Paint()
        otherCircleOuterPaint.isAntiAlias = true
        otherCircleOuterPaint.color = Color.parseColor("#C5B170")
        otherCircleOuterPaint.style = Paint.Style.STROKE
        otherCircleOuterPaint.strokeWidth = dip2px(1f)
        otherCircleInnerPaint = Paint()
        otherCircleInnerPaint.isAntiAlias = true
        otherCircleInnerPaint.color = Color.parseColor("#F3EDD6")
        otherCircleInnerPaint.style = Paint.Style.FILL
        centerWordsPaint = Paint()
        centerWordsPaint.style = Paint.Style.STROKE
        centerWordsPaint.isAntiAlias = true
        centerWordsPaint.color = Color.parseColor("#ffffff")
        centerWordsPaint.textSize = 50f

        centerWordsPaint.textAlign = Paint.Align.CENTER
        otherWordsPaint = Paint()
        otherWordsPaint.style = Paint.Style.STROKE
        otherWordsPaint.isAntiAlias = true
        otherWordsPaint.color = Color.parseColor("#C5B170")
        otherWordsPaint.textSize = 40f
        otherWordsPaint.textAlign = Paint.Align.CENTER
        textRect = Rect()

    }

    private fun initPath() {

        if (stories.size >= 1) {
            val path1 = makeLine(-5, intArrayOf(52, -60, -9), true, intArrayOf(-2, -2))
            path1.endDirection = ShipPath.EDotDirection.LEFT
            paths.add(path1)
        }
        if (stories.size >= 2) {
            val path2 = makeLine(0, intArrayOf(-76, 31, -43), true, intArrayOf(-2, 2))
            path2.endDirection = ShipPath.EDotDirection.LEFT
            paths.add(path2)
        }
        if (stories.size >= 3) {
            val path3 = makeLine(0, intArrayOf(49, -52, 22), true, intArrayOf(2, -2))
            path3.endDirection = ShipPath.EDotDirection.RIGHT
            paths.add(path3)
        }
        if (stories.size >= 4) {
            val path4 = makeLine(-7, intArrayOf(51, -54, 12), false, intArrayOf(2, -2))
            path4.endDirection = ShipPath.EDotDirection.BOTTOM
            paths.add(path4)
        }
        if (stories.size >= 5) {
            val path5 = makeLine(5, intArrayOf(60, 37, 34, 38), false, intArrayOf(2, 2, 2))
            path5.endDirection = ShipPath.EDotDirection.RIGHT
            paths.add(path5)
        }

        if (stories.size >= 6) {
            val path6 = makeLine(6, intArrayOf(60, 37, 34, 60), false, intArrayOf(2, 2, 2))
            path6.endDirection = ShipPath.EDotDirection.RIGHT
            paths.add(path6)
        }

        val max = 34
        val min = 20
        for (path in paths) {
            val pathMeasure = PathMeasure(path, false)
            pathMeasures.add(pathMeasure)
            val random = Random()
            val s = random.nextInt(max) % (max - min + 1) + min
            radiusArray.add(s)
        }

        drawLine(false)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpec = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightSpec = View.MeasureSpec.getMode(heightMeasureSpec)
        if (widthSpec == View.MeasureSpec.EXACTLY) {
            mwidth = View.MeasureSpec.getSize(widthMeasureSpec)
        } else {
            mwidth = 200
        }
        if (heightSpec == View.MeasureSpec.EXACTLY) {
            mheight = View.MeasureSpec.getSize(heightMeasureSpec)
        } else {
            mheight = 200
        }
        x = mwidth / 2
        y = mheight / 2

        setMeasuredDimension(mwidth, mheight)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(x.toFloat(), y.toFloat())

        for (i in 0 until pathMeasures.size) {
            val path1 = Path()
            val pathMeasure = pathMeasures[i]
            val path = paths[i]
            if (!isReverse) {
                pathMeasure.getSegment(0f, pathMeasure.length * animatorValue, path1, true)
                canvas.drawPath(path1, linePaint)
            } else {
                pathMeasure.getSegment(0f, pathMeasure.length * (1 - animatorValue), path1, true)
                canvas.drawPath(path1, linePaint)
            }

            drawCircles(radiusArray[i], canvas, path.endPoint, path.endDirection, i)
        }

        drawCenterCircle(canvas)
    }

    /**
     * 绘制中心View
     */
    private fun drawCenterCircle(canvas: Canvas) {
        middleCirclePaint.style = Paint.Style.FILL
        canvas.drawCircle(0f, 0f, dip2px(30f), middleCirclePaint)
        middleCirclePaint.style = Paint.Style.STROKE
        middleCirclePaint.strokeWidth = dip2px(3f)
        canvas.drawCircle(0f, 0f, dip2px(34f), middleCirclePaint)

        var centerName = "我"
        centerWordsPaint.getTextBounds(centerName, 0, centerName.length - 1, textRect)
        canvas.drawText(centerName, 0f, (0 + textRect.height() / 2).toFloat(), centerWordsPaint)

        if (isReverse) {
            middleCirclePaint.alpha = (255 * (1 - animatorValue)).toInt()
            centerWordsPaint.alpha = (255 * (1 - animatorValue)).toInt()
        } else {
            middleCirclePaint.alpha = (animatorValue * 255).toInt()
            centerWordsPaint.alpha = (255 * animatorValue).toInt()
        }
    }

    /**
     * 绘制外圈View
     */
    private fun drawCircles(aRadius: Int?, canvas: Canvas, centerPoint: Point, endDirection: ShipPath.EDotDirection, i: Int) {
        val point = Point(centerPoint.x, centerPoint.y)
        val radius = dip2px(aRadius!!.toFloat())
        when (endDirection) {
            ShipPath.EDotDirection.LEFT -> point.x -= radius.toInt()
            ShipPath.EDotDirection.RIGHT -> point.x += radius.toInt()
            ShipPath.EDotDirection.BOTTOM -> point.y += radius.toInt()
            ShipPath.EDotDirection.TOP -> point.y -= radius.toInt()
        }
        //周围圆圆心point.x, point.y
        stories[i].point = point
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), radius - dip2px(4f), otherCircleInnerPaint)

        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), radius, otherCircleOuterPaint)

        var name = "朋友"

        otherWordsPaint.getTextBounds(name, 0, name.length - 1, textRect)
        canvas.drawText(name, point.x.toFloat(), (point.y + textRect.height() / 2).toFloat(), otherWordsPaint)
        if (isReverse) {
            otherCircleInnerPaint.alpha = (255 * (1 - animatorValue)).toInt()
            otherCircleOuterPaint.alpha = (255 * (1 - animatorValue)).toInt()
            otherWordsPaint.alpha = (255 * (1 - animatorValue)).toInt()
        } else {
            otherCircleInnerPaint.alpha = (255 * animatorValue).toInt()
            otherCircleOuterPaint.alpha = (255 * animatorValue).toInt()
            otherWordsPaint.alpha = (255 * animatorValue).toInt()
        }

    }

    fun dip2px(dipValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dipValue * scale + 0.5f
    }

    /**
     * 画线
     */
    fun drawLine(reverse: Boolean) {

        isReverse = reverse
        if (!isReverse) {
            setPaintColor()
        }
        val valueAnimator = ObjectAnimator.ofFloat(0f, 1f).setDuration(600)
        valueAnimator.addUpdateListener({ animation ->
            animatorValue = animation.animatedValue as Float
            //刷新界面：invalidate()在主线程、postInvalidate()在工作线程
            postInvalidate()
        })
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                if (clickItem !== -1) {
                    onClickStoryListener.onClickStory(stories[clickItem])
                    clickItem = -1
                }
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        valueAnimator.start()
    }


    fun makeLine(start: Int, controlPoint: IntArray, isHorizontal: Boolean, additional: IntArray): ShipPath {
        var isHorizontal = isHorizontal
        val myPath = ShipPath(context)

        var endPoint = myPath.forward(start, controlPoint[0], isHorizontal)
        isHorizontal = !isHorizontal
        for (i in additional.indices) {
            if (isHorizontal) {
                endPoint = myPath.forwardWithCircle(endPoint,
                        Point(
                                (endPoint.x + dip2px(controlPoint[i + 1].toFloat())).toInt(),
                                (endPoint.y + dip2px(additional[i].toFloat())).toInt()),
                        isHorizontal)
            } else {
                endPoint = myPath.forwardWithCircle(endPoint,
                        android.graphics.Point((endPoint.x + dip2px(additional[i].toFloat())).toInt(),
                                (endPoint.y + dip2px(controlPoint[i + 1].toFloat())).toInt()),
                        isHorizontal)
            }
            myPath.drawLine(null, endPoint)
            isHorizontal = !isHorizontal
        }
        myPath.endPoint = endPoint
        return myPath
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val upX = event.x.toInt()
                val upY = event.y.toInt()
                for (i in 0 until stories.size) {
                    val mRadius = radiusArray[i] + 20
                    val x = stories[i].point!!.x
                    val y = stories[i].point!!.y
                    val realX: Int
                    val realY: Int
                    realX = width / 2 + x
                    realY = height / 2 + y
                    if (upX >= realX - mRadius && upX <= realX + mRadius && upY >= realY - mRadius && upY <= realY + mRadius) {
                        clickItem = i
                        break
                    }
                }
            }
            MotionEvent.ACTION_UP -> if (clickItem !== -1) {

                drawLine(true)

            }

        }
        return true
    }

    fun setStories(stories: ArrayList<Ship>, centerStory: Ship) {
        paths.clear()
        pathMeasures.clear()
        radiusArray.clear()
        this.stories = stories
        this.centerStory = centerStory
        initPath()
    }

    fun setOnShipClickListener(onClickStoryListener: OnShipClickListener) {
        this.onClickStoryListener = onClickStoryListener
    }

    private fun setPaintColor() {
        count++
        val random = Random()
        val index = random.nextInt(3)
        when (count % 3) {
            0 -> {
                middleCirclePaint.color = Color.parseColor("#F8D46F")
                linePaint.color = Color.parseColor("#C5B170")
                otherCircleOuterPaint.color = Color.parseColor("#C5B170")
                otherCircleInnerPaint.color = Color.parseColor("#F3EDD6")
                otherWordsPaint.color = Color.parseColor("#C5B170")
            }
            1 -> {
                middleCirclePaint.color = Color.parseColor("#A0A5FB")
                linePaint.color = Color.parseColor("#A0A5FB")
                otherCircleOuterPaint.color = Color.parseColor("#A0A5FB")
                otherCircleInnerPaint.color = Color.parseColor("#D6EAF3")
                otherWordsPaint.color = Color.parseColor("#7F83C1")
            }
            2 -> {
                middleCirclePaint.color = Color.parseColor("#FBB8A0")
                linePaint.color = Color.parseColor("#FBB8A0")
                otherCircleOuterPaint.color = Color.parseColor("#FBB8A0")
                otherCircleInnerPaint.color = Color.parseColor("#FFEBE0")
                otherWordsPaint.color = Color.parseColor("#CD7252")
            }
        }
        middleCirclePaint.alpha = 0
        otherCircleOuterPaint.alpha = 0
        otherCircleInnerPaint.alpha = 0
        otherWordsPaint.alpha = 0
    }
}