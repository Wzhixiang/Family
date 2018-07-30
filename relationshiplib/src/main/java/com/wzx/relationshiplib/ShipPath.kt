package com.wzx.relationshiplib

import android.graphics.Path
import android.content.Context
import android.graphics.Point
import android.graphics.RectF


/**
 * 描述：
 *
 * 创建人： Administrator
 * 创建时间： 2018/7/24
 * 更新时间：
 * 更新内容：
 */
class ShipPath(context: Context) : Path() {

    enum class EDotDirection constructor(value: Int) {
        LEFT(0), TOP(90), RIGHT(180), BOTTOM(270);

        var value: Int = value
    }


    private var mContext: Context = context

    var endPoint: Point

    init {
        endPoint = Point()
    }

    lateinit var endDirection: EDotDirection


    fun forward(start: Int, pointOne: Int, isHorizontal: Boolean): Point {
        var start = start
        var pointOne = pointOne
        var distance = Math.sqrt(Math.pow(34.0, 2.0) - Math.pow(start.toDouble(), 2.0)).toInt()
        distance = dip2px(distance.toFloat())
        if (pointOne < 0) {
            distance = -distance
        }
        val startX: Int
        val startY: Int
        val pointOneX: Int
        val pointOneY: Int
        start = dip2px(start.toFloat())
        pointOne = dip2px(pointOne.toFloat())

        if (isHorizontal) {
            startX = distance
            startY = start
            pointOneX = pointOne
            pointOneY = startY
        } else {
            startX = start
            startY = distance
            pointOneX = startX
            pointOneY = pointOne
        }
        this.moveTo(startX.toFloat(), startY.toFloat())
        this.lineTo(pointOneX.toFloat(), pointOneY.toFloat())
        endPoint = Point(pointOneX, pointOneY)
        return endPoint
    }

    fun forwardWithCircle(startPoint: Point, endPoint: Point, isHorizontal: Boolean): Point {
        var dotDirectionIn = EDotDirection.LEFT
        var dotDirectionOut = EDotDirection.LEFT
        if (isHorizontal) {
            if (startPoint.x > endPoint.x && startPoint.y < endPoint.y) {
                dotDirectionIn = EDotDirection.TOP
                dotDirectionOut = EDotDirection.LEFT
            } else if (startPoint.x < endPoint.x && startPoint.y < endPoint.y) {
                dotDirectionIn = EDotDirection.TOP
                dotDirectionOut = EDotDirection.RIGHT
            } else if (startPoint.x > endPoint.x && startPoint.y > endPoint.y) {
                dotDirectionIn = EDotDirection.BOTTOM
                dotDirectionOut = EDotDirection.LEFT
            } else if (startPoint.x < endPoint.x && startPoint.y > endPoint.y) {
                dotDirectionIn = EDotDirection.BOTTOM
                dotDirectionOut = EDotDirection.RIGHT
            }
        } else {
            if (startPoint.x > endPoint.x && startPoint.y < endPoint.y) {
                dotDirectionIn = EDotDirection.RIGHT
                dotDirectionOut = EDotDirection.BOTTOM
            } else if (startPoint.x < endPoint.x && startPoint.y < endPoint.y) {
                dotDirectionIn = EDotDirection.LEFT
                dotDirectionOut = EDotDirection.BOTTOM
            } else if (startPoint.x > endPoint.x && startPoint.y > endPoint.y) {
                dotDirectionIn = EDotDirection.RIGHT
                dotDirectionOut = EDotDirection.TOP
            } else if (startPoint.x < endPoint.x && startPoint.y > endPoint.y) {
                dotDirectionIn = EDotDirection.LEFT
                dotDirectionOut = EDotDirection.TOP
            }
        }

        drawCircle(startPoint, endPoint, dotDirectionIn, dotDirectionOut)
        this.endPoint = endPoint
        return endPoint
    }


    fun drawCircle(startPoint: Point, endPoint: Point, dotDirectionIn: EDotDirection, dotDirectionOut: EDotDirection) {
        val centerPoint: Point
        if (dotDirectionIn == EDotDirection.TOP || dotDirectionIn == EDotDirection.BOTTOM) {
            centerPoint = Point(startPoint.x, endPoint.y)

        } else {
            centerPoint = Point(endPoint.x, startPoint.y)

        }

        val rectF = RectF(centerPoint.x - dip2px(2f).toFloat(),
                centerPoint.y - dip2px(2f).toFloat(),
                centerPoint.x + dip2px(2f).toFloat(),
                centerPoint.y + dip2px(2f).toFloat())
        this.arcTo(rectF, dotDirectionIn.value.toFloat(),
                (dotDirectionOut.value - dotDirectionIn.value).toFloat())
        this.arcTo(rectF, dotDirectionOut.value.toFloat(), 359.9f)


    }

    fun drawLine(startPoint: Point?, endPoint: Point) {
        if (startPoint != null) {
            this.moveTo(startPoint!!.x.toFloat(), startPoint!!.y.toFloat())
        }
        this.lineTo(endPoint.x.toFloat(), endPoint.y.toFloat())
    }

    fun dip2px(dipValue: Float): Int {
        val scale = mContext.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}