package com.xodus.weatheranimation

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.sin

class WeatherAnimation(private val parent: ViewGroup) {
    class FallItem(var view: View, var size: Int, var xMove: Float, var yMove: Float) {
        enum class FallType {
            RAIN,
            SNOW,
            CLOUD,
            CLOUD_DARK,
        }
    }

    enum class AnimationState {
        PLAY,
        PAUSE,
        STOP,
        END,
    }

    private val appClass = parent.context
    private var animator: ValueAnimator = ValueAnimator.ofInt(0, 100)
    private val itemList = arrayListOf<FallItem>()

    private var count = 0
    private var speed = 0F
    private var direction = 0
    private var type = FallItem.FallType.SNOW
    private var state = AnimationState.STOP
    private var hasClouds = false
    val animationState
        get() = state

    init {
        animator.duration = 5000L
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()
    }

    fun start(): WeatherAnimation {
        if (state == AnimationState.STOP) {
            state = AnimationState.PLAY
            parent.visibility = View.VISIBLE
            animator.start()
        }
        return this
    }

    fun pause(): WeatherAnimation {
        if (state == AnimationState.PLAY) {
            state = AnimationState.PAUSE
            animator.pause()
        }
        return this
    }

    fun resume(): WeatherAnimation {
        if (state == AnimationState.PAUSE) {
            state = AnimationState.PLAY
            animator.resume()
        }
        return this
    }

    fun stop(): WeatherAnimation {
        if (state != AnimationState.STOP) {
            state = AnimationState.END
            animator.end()
            parent.removeAllViews()
            itemList.clear()
            parent.visibility = View.GONE
        }
        return this
    }

    fun lightning(count: Int = 10, hasClouds: Boolean = false): WeatherAnimation {
        this.count = count
        this.hasClouds = hasClouds
        parent.removeAllViews()
        itemList.clear()
        parent.post {
            animator.addUpdateListener {
                if ((0..10).random() == 0) {
                    if ((0..100).random() < count) {
                        strike()
                    }
                }
            }
        }
        state = AnimationState.STOP
        parent.visibility = View.GONE
        return this
    }

    private fun strike() {
        val height = parent.height
        val width = parent.width
        val item = View(appClass)
        val drawable = when ((1..3).random()) {
            1    -> ContextCompat.getDrawable(appClass, R.drawable.weather_lightning_1)
            2    -> ContextCompat.getDrawable(appClass, R.drawable.weather_lightning_2)
            3    -> ContextCompat.getDrawable(appClass, R.drawable.weather_lightning_3)
            4    -> ContextCompat.getDrawable(appClass, R.drawable.weather_lightning_4)
            5    -> ContextCompat.getDrawable(appClass, R.drawable.weather_lightning_5)
            else -> ContextCompat.getDrawable(appClass, R.drawable.weather_lightning_6)
        }
        item.background = drawable
        item.scaleX = if ((0..1).random() == 0) -1F else 1F
        parent.addView(item)
        val param = item.layoutParams
        val size = (height / 10 until height / 2).random()
        param.width = size
        param.height = size * (drawable?.intrinsicHeight ?: 0) / (drawable?.intrinsicWidth ?: 0)
        item.x = (0 until width).random().toFloat()
        if (hasClouds) {
            item.y = size * (height / 3F) / (height / 2F) //change y based on size to match better with clouds
        } else {
            item.y = 0F
        }

        item.rotation = (-10 until 10).random().toFloat()
        if ((0..1).random() == 0) {
            item.animate().alpha(0F).setDuration(0).setStartDelay(0).withEndAction {
                item.animate().alpha(1F).setDuration(10).setStartDelay(0).withEndAction {
                    item.animate().alpha(0.5F).setDuration(50).setStartDelay(50).withEndAction {
                        item.animate().alpha(1F).setDuration(10).setStartDelay(0).withEndAction {
                            item.animate().alpha(0F).setDuration(500).setStartDelay(300).withEndAction {
                                parent.removeView(item)
                            }.start()
                        }.start()
                    }.start()
                }.start()
            }.start()
        } else {
            item.animate().alpha(0F).setDuration(0).setStartDelay(0).withEndAction {
                item.animate().alpha(1F).setDuration(10).setStartDelay(0).withEndAction {
                    item.animate().alpha(0F).setDuration(500).setStartDelay(300).withEndAction {
                        parent.removeView(item)
                    }.start()
                }.start()
            }.start()
        }
    }

    fun fall(count: Int = 200, speed: Float = 5F, direction: Int = 90, type: FallItem.FallType = FallItem.FallType.SNOW): WeatherAnimation {
        this.count = count
        this.speed = speed
        this.direction = direction
        this.type = type
        this.state = AnimationState.STOP

        val itemList = arrayListOf<FallItem>()
        if (direction > 180 || direction < 0) {
            log("Weather Animation Error : Invalid Direction. Must be between 0 and 180.")
            return this
        }
        if (speed < 0) {
            log("Weather Animation Error : Invalid Speed. Must be greater than 0.")
            return this
        }
        parent.removeAllViews()
        itemList.clear()

        val ang = deg2rad(direction.toDouble())
        val cos = cos(ang).toFloat()
        val sin = sin(ang).toFloat()
        val toLeft = direction > 90
        val is90 = direction == 90

        parent.post {
            val height = parent.height
            val width = parent.width
            val a = cos * width
            val x1 = (cos * a).toInt()
            val y1 = (sin * a).toInt()
            val a2 = sin * height
            val x2 = (cos * a2).toInt()
            val y2 = (sin * a2).toInt()

            val rx1 = if (toLeft) x1 else width - x1
            val ry1 = if (toLeft) y1 else y1 * -1
            val rx2 = if (toLeft) (x2 * -1) + width else x2 * -1
            val ry2 = height - y2

            val distX = rx2 - rx1
            val distY = ry2 - ry1
            repeat(count) {
                val item = View(appClass)
                item.tag = it
                when (type) {
                    FallItem.FallType.RAIN       -> {
                        item.background = getShape(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            ContextCompat.getColor(appClass, R.color.md_blue_100),
                            ContextCompat.getColor(appClass, R.color.md_transparent_1000), 10
                        )
                    }
                    FallItem.FallType.SNOW       -> {
                        item.background = ContextCompat.getDrawable(appClass, R.drawable.weather_snowflake)
                        item.backgroundTintList = ContextCompat.getColorStateList(appClass, R.color.md_blue_200)
                    }
                    FallItem.FallType.CLOUD,
                    FallItem.FallType.CLOUD_DARK -> Unit
                }
                parent.addView(item)
                val param = item.layoutParams
                var size = 0
                var xMove = 0F
                var yMove = 0F
                when (type) {
                    FallItem.FallType.RAIN       -> {
                        size = convertDPtoPX(20F + (speed * 2F)).toInt()
                        param.width = convertDPtoPX(if ((0..5).random() == 0) 2F else 1F).toInt()
                        param.height = size
                        item.x = (0 until width).random().toFloat()
                        item.y = (0..height).random().toFloat()
                        item.rotation = direction.toFloat() - 90
                        xMove = (speed + 15) * cos
                        yMove = (speed + 15) * sin
                    }
                    FallItem.FallType.SNOW       -> {
                        val randomArray = arrayListOf<Int>()
                        repeat(16) { value ->
                            repeat(value * 3) {
                                randomArray.add(16 - value)
                            }
                        }
                        size = convertDPtoPX(4F + randomArray[(0 until randomArray.size).random()].toFloat()).toInt()
                        param.width = size
                        param.height = size

                        item.x = (0 until width).random().toFloat()
                        item.y = (0 until height).random().toFloat()
                        xMove = if (is90) {
                            val change = ((0..20).random().toFloat() + 20F - (sin * 30F)) / 80F
                            size * speed * cos / 40F + speed * change
                        } else {
                            size * speed * cos / 40F
                        }
                        yMove = size * speed * sin / 40F
                    }
                    FallItem.FallType.CLOUD,
                    FallItem.FallType.CLOUD_DARK -> {
                        val drawable = when ((1..5).random()) {
                            1    -> ContextCompat.getDrawable(appClass, if (type == FallItem.FallType.CLOUD_DARK) R.drawable.weather_cloud_grey_1 else R.drawable.weather_cloud_1)
                            2    -> ContextCompat.getDrawable(appClass, if (type == FallItem.FallType.CLOUD_DARK) R.drawable.weather_cloud_grey_2 else R.drawable.weather_cloud_2)
                            3    -> ContextCompat.getDrawable(appClass, if (type == FallItem.FallType.CLOUD_DARK) R.drawable.weather_cloud_grey_3 else R.drawable.weather_cloud_3)
                            4    -> ContextCompat.getDrawable(appClass, if (type == FallItem.FallType.CLOUD_DARK) R.drawable.weather_cloud_grey_4 else R.drawable.weather_cloud_4)
                            else -> ContextCompat.getDrawable(appClass, if (type == FallItem.FallType.CLOUD_DARK) R.drawable.weather_cloud_grey_5 else R.drawable.weather_cloud_5)
                        }
                        item.background = drawable
                        val randomArray = arrayListOf<Int>()
                        repeat(10) { value ->
                            repeat(value * 3) {
                                randomArray.add(11 - value)
                            }
                        }
                        size = width / randomArray[(0 until randomArray.size).random()]
                        val h = size * (drawable?.intrinsicHeight ?: 0) / (drawable?.intrinsicWidth ?: 0)
                        param.width = size
                        param.height = h
                        item.x = (0 until width).random().toFloat()
                        item.y = (0 until height / 3 - h).random().toFloat()
                        xMove = if (is90) {
                            val change = ((0..20).random().toFloat() + 20F - (sin * 30F)) / 40F
                            ((size / 100F * speed * cos) + (speed * change)) / 10F
                        } else {
                            size / 1000F * speed * cos
                        }
                        yMove = 0F
                    }
                }
                itemList.add(FallItem(item, size, xMove, yMove))
            }
            animator.addUpdateListener {
                itemList.forEach { item ->
                    item.view.x = item.view.x + item.xMove
                    item.view.y = item.view.y + item.yMove
                    if (
                        item.view.y > height ||
                        (toLeft && item.view.x < -item.size) ||
                        (toLeft.not() && item.view.x > width + item.size) ||
                        (arrayOf(FallItem.FallType.CLOUD, FallItem.FallType.CLOUD_DARK).contains(type) && is90 && ((item.xMove > 0 && item.view.x > width + item.size) || (item.xMove < 0 && item.view.x < -item.size)))
                    ) {
                        when (type) {
                            FallItem.FallType.RAIN       -> {
                                val rand = (0..100).random().toFloat() / 100F
                                item.view.x = (distX * rand) + rx1
                                item.view.y = (distY * rand) + ry1
                            }
                            FallItem.FallType.SNOW       -> {
                                if (is90) {
                                    val change = ((0..20).random().toFloat() + 20F - (sin * 30F)) / 80F
                                    item.xMove = item.size * speed * cos / 40F + speed * change
                                }
                                val rand = (0..100).random().toFloat() / 100F
                                item.view.x = (distX * rand) + rx1
                                item.view.y = (distY * rand) + ry1
                            }
                            FallItem.FallType.CLOUD,
                            FallItem.FallType.CLOUD_DARK -> {
                                val change = ((0..20).random().toFloat() + 20F - (sin * 30F)) / 40F
                                if (is90) {
                                    item.xMove = ((item.size / 100F * speed * cos) + (speed * change)) / 10F
                                }
                                if (is90) {
                                    if (change > 0) {
                                        item.view.x = -item.size.toFloat()
                                    } else {
                                        item.view.x = width.toFloat()
                                    }
                                } else {
                                    item.view.x = if (toLeft) width.toFloat() else -item.size.toFloat()
                                }
                                item.view.y = (0..height / 3).random().toFloat()
                            }
                        }
                    }
                }
            }

        }
        state = AnimationState.STOP
        parent.visibility = View.GONE
        return this
    }

    private fun deg2rad(deg: Double): Double {
        return (deg * Math.PI / 180.0)
    }

    private fun convertDPtoPX(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, appClass.resources.displayMetrics)
    }

    private fun getShape(
        orientation: GradientDrawable.Orientation,
        firstColor: Int,
        secondColor: Int,
        borderColor: Int,
        borderWidth: Int,
        topLeftRadiusX: Int,
        topLeftRadiusY: Int,
        topRightRadiusX: Int,
        topRightRadiusY: Int,
        downRightRadiusX: Int,
        downRightRadiusY: Int,
        downLeftRadiusX: Int,
        downLeftRadiusY: Int
    ): GradientDrawable {
        val shape = GradientDrawable(orientation, intArrayOf(firstColor, secondColor))
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadii = floatArrayOf(
            convertDPtoPX(topLeftRadiusX.toFloat()),
            convertDPtoPX(topLeftRadiusY.toFloat()),
            convertDPtoPX(topRightRadiusX.toFloat()),
            convertDPtoPX(topRightRadiusY.toFloat()),
            convertDPtoPX(downRightRadiusX.toFloat()),
            convertDPtoPX(downRightRadiusY.toFloat()),
            convertDPtoPX(downLeftRadiusX.toFloat()),
            convertDPtoPX(downLeftRadiusY.toFloat())
        )
        shape.setStroke(borderWidth, borderColor)
        return shape
    }

    private fun getShape(
        orientation: GradientDrawable.Orientation,
        firstColor: Int,
        secondColor: Int,
        borderColor: Int,
        borderWidth: Int,
        topLeftRadius: Int,
        topRightRadius: Int,
        downRightRadius: Int,
        downLeftRadius: Int
    ): GradientDrawable {
        return getShape(
            orientation,
            firstColor,
            secondColor,
            borderColor,
            borderWidth,
            topLeftRadius,
            topLeftRadius,
            topRightRadius,
            topRightRadius,
            downRightRadius,
            downRightRadius,
            downLeftRadius,
            downLeftRadius
        )
    }


    private fun getShape(
        orientation: GradientDrawable.Orientation,
        firstColor: Int,
        secondColor: Int,
        radius: Int
    ): GradientDrawable {
        return getShape(
            orientation,
            firstColor,
            secondColor,
            R.color.md_transparent_1000,
            0,
            radius,
            radius,
            radius,
            radius
        )
    }


    private fun getStringLog(vararg s: Any?): String {
        val value = StringBuilder()
        for (item in s) {
            value.append(item.toString()).append("\n")
        }
        return value.toString()
    }

    private fun log(vararg s: Any?) {
        if (BuildConfig.DEBUG) {
            val length = 3500
            val location = getLocation(3)
            var result = getStringLog(*s)
            while (result.isNotEmpty()) {
                result = if (result.length >= length) {
                    Log.e(location, result.substring(0, length))
                    result.substring(length, result.length)
                } else {
                    Log.e(location, result)
                    ""
                }
            }
        }
    }

    private fun getLocation(index: Int): String {
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size <= index) {
            throw IllegalStateException(
                "Synthetic stacktrace didn't have enough elements: are you using proguard?"
            )
        }
        //        for (int i = 0; i < stackTrace.length; i++) {
        //            log(LOG, "TRACETRACE INDEX=" + i + "|" + stackTrace[i].toString());
        //        }


        return ".(" + stackTrace[index].toString().substring(
            stackTrace[index].toString().lastIndexOf("(") + 1,
            stackTrace[index].toString().lastIndexOf(")")
        ) + ")"
    }
}