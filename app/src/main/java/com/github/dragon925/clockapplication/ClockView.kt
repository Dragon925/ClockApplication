package com.github.dragon925.clockapplication

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.*

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class NumberType {ARABIC, ROMAN}

    companion object {
        private const val DEFAULT_BORDER_WIDTH = 10
        private const val DEFAULT_SIZE = 100
        private const val DEFAULT_COLOR = Color.BLACK
        private const val DEFAULT_BACKGROUND_COLOR = Color.WHITE
        private const val DEFAULT_ARROW_WIDTH = 9
    }

    private var needUpdate = false

    @Px
    var borderWidth: Float = context.dpToPx(DEFAULT_BORDER_WIDTH)
    set(value) {
        field = value
        if (needUpdate) updateBackground()
    }
    @Px
    var borderMaxWidth: Float = context.dpToPx(DEFAULT_SIZE / 10)
    private set
    @ColorInt
    var borderColor: Int = DEFAULT_COLOR
    set(value) {
        field = value
        if (needUpdate) updateBackground()
    }
    @ColorInt
    var numberColor: Int = DEFAULT_COLOR
    set(value) {
        field = value
        if (needUpdate) updateBackground()
    }
    @ColorInt
    var diamondColor: Int = DEFAULT_COLOR
    set(value) {
        field = value
        if (needUpdate) updateBackground()
    }
    @ColorInt
    var clockFaceColor: Int = DEFAULT_BACKGROUND_COLOR
    set(value) {
        field = value
        if (needUpdate) updateBackground()
    }
    @ColorInt
    var secondHandColor: Int = DEFAULT_COLOR
    set(value) {
        field = value
        if (needUpdate) invalidate()
    }
    @ColorInt
    var minuteHandColor: Int = DEFAULT_COLOR
    set(value) {
        field = value
        if (needUpdate) invalidate()
    }
    @ColorInt
    var hourHandColor: Int = DEFAULT_COLOR
    set(value) {
        field = value
        if (needUpdate) invalidate()
    }

    var hasNumbers: Boolean = true
    set(value) {
        field = value
        if (needUpdate) updateBackground()
    }
    var hasDiamonds: Boolean = true
    set(value) {
        field = value
        if (needUpdate) updateBackground()
    }
    var numberType: NumberType = NumberType.ARABIC
    set(value) {
        field = value
        if (needUpdate) updateBackground()
    }

    private val viewRect = Rect()
    private val borderRect = RectF()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        strokeWidth = borderWidth
        color = DEFAULT_BACKGROUND_COLOR
    }
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_COLOR
        strokeWidth = arrowWidth
    }

    private val time = Calendar.getInstance()
    var timeZone: TimeZone = time.timeZone
    set(value) {
        field = value
        time.timeZone = value
        if (needUpdate) invalidate()
    }
    private val background = Picture()

    private var arrowWidth = context.dpToPx(DEFAULT_ARROW_WIDTH)
    private var contentRadius: Float = (context.dpToPx(DEFAULT_SIZE) / 2f - borderWidth) * 0.85f

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.ClockView, defStyleAttr, 0)) {
            borderWidth = abs(
                getDimension(R.styleable.ClockView_borderWidth, context.dpToPx(DEFAULT_BORDER_WIDTH))
            )
            borderColor = getColor(R.styleable.ClockView_borderColor, DEFAULT_COLOR)
            numberColor = getColor(R.styleable.ClockView_numberColor, DEFAULT_COLOR)
            diamondColor = getColor(R.styleable.ClockView_diamondColor, DEFAULT_COLOR)
            clockFaceColor = getColor(R.styleable.ClockView_clockFaceColor, DEFAULT_BACKGROUND_COLOR)
            val color = getColor(R.styleable.ClockView_clockHandColor, DEFAULT_COLOR)
            secondHandColor = getColor(R.styleable.ClockView_secondHandColor, color)
            minuteHandColor = getColor(R.styleable.ClockView_minuteHandColor, color)
            hourHandColor = getColor(R.styleable.ClockView_hourHandColor, color)

            hasNumbers = getBoolean(R.styleable.ClockView_hasNumbers, true)
            hasDiamonds = getBoolean(R.styleable.ClockView_hasDiamonds, true)

            val type = getInteger(R.styleable.ClockView_numbersType, 0)
            numberType = NumberType.values().getOrNull(type) ?: NumberType.ARABIC

            val zone = getInteger(R.styleable.ClockView_timeZone, -1)
            if (zone != -1) {
                val sign = if (zone < 12) "-" else "+"
                val offset = abs(zone % 25 - 12)
                timeZone = TimeZone.getTimeZone("GMT$sign$offset")
            }

            recycle()
        }
        needUpdate = true
    }

    fun setClockHandColor(@ColorInt color: Int) {
        withUpdateLock {
            secondHandColor = color
            minuteHandColor = color
            hourHandColor = color
        }
        invalidate()
    }

    fun setClockColor(@ColorInt color: Int) {
        withUpdateLock {
            borderColor = color
            numberColor = color
            diamondColor = color
            secondHandColor = color
            minuteHandColor = color
            hourHandColor = color
        }
        updateBackground()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        scope.launch {
            while (true) {
                delay(1000)
                invalidate()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.coroutineContext.cancelChildren()
    }

    override fun onSaveInstanceState(): Parcelable = SaveState(super.onSaveInstanceState()).also {
        it.borderWidth = borderWidth
        it.borderColor = borderColor
        it.textColor = numberColor
        it.diamondColor = diamondColor
        it.clockFaceColor = clockFaceColor
        it.secondHandColor = secondHandColor
        it.minuteHandColor = minuteHandColor
        it.hourHandColor = hourHandColor
        it.hasNumbers = hasNumbers
        it.hasDiamonds = hasDiamonds
        it.numberType = numberType
        it.timeZone = timeZone
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state !is SaveState) return

        withUpdateLock {
            borderWidth = state.borderWidth
            borderColor = state.borderColor
            numberColor = state.textColor
            diamondColor = state.diamondColor
            clockFaceColor = state.clockFaceColor
            secondHandColor = state.secondHandColor
            minuteHandColor = state.minuteHandColor
            hourHandColor = state.hourHandColor
            hasNumbers = state.hasNumbers
            hasDiamonds = state.hasDiamonds
            numberType = state.numberType
            timeZone = state.timeZone
        }
        updateBackground()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val resolvedWidth = resolveDefaultSize(widthMeasureSpec)
        val resolvedHeight = resolveDefaultSize(heightMeasureSpec)

        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    private fun resolveDefaultSize(spec: Int): Int {
        return if (MeasureSpec.getMode(spec) == MeasureSpec.UNSPECIFIED) {
            context.dpToPx(DEFAULT_SIZE).toInt()
        } else {
            MeasureSpec.getSize(spec)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return

        calculateBounds(w, h)

        prepareBackground(viewRect)
    }

    private fun calculateBounds(width: Int, height: Int) {
        val availableWidth = width - paddingStart - paddingEnd
        val availableHeight = height - paddingTop - paddingBottom
        val sideSize = min(availableWidth, availableHeight)

        val x = paddingStart + (availableWidth - sideSize) / 2
        val y = paddingTop + (availableHeight - sideSize) / 2

        viewRect.apply {
            left = x
            top = y
            right = x + sideSize
            bottom = y + sideSize
        }
        borderMaxWidth = sideSize * 0.1f
        arrowWidth = sideSize * 0.03f
    }

    private fun updateBackground() {
        prepareBackground(viewRect)
        invalidate()
    }

    private fun prepareBackground(rect: Rect) {

        val borderWidth = min(this.borderWidth, borderMaxWidth)
        val half = borderWidth / 2
        borderRect.set(rect)
        borderRect.inset(half, half)

        val width = borderRect.width()
        val centerX = rect.exactCenterX()
        val centerY = rect.exactCenterY()

        val k = if (hasDiamonds) 0.85f else 0.95f
        contentRadius = (width / 2f - borderWidth) * k
        val diamondCircleRadius = contentRadius / 2 + width / 4f

        val canvas = background.beginRecording(rect.width(), rect.height())

        canvas.drawOval(borderRect, bgPaint.apply {
            style = Paint.Style.FILL
            color = clockFaceColor
        })

        if (borderWidth != 0f) {
            canvas.drawOval(borderRect, bgPaint.apply {
                strokeWidth = borderWidth
                style = Paint.Style.STROKE
                color = borderColor
            })
        }

        if (hasNumbers) drawNumbersCircle(canvas, centerX, centerY, contentRadius, bgPaint.apply {
            style = Paint.Style.FILL
            textSize = width * 0.1f
            color = numberColor
        })

        if (hasDiamonds) drawDiamondCircle(canvas, centerX, centerY, diamondCircleRadius, bgPaint.apply {
            style = Paint.Style.FILL
            color = diamondColor
        })

        background.endRecording()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        canvas.drawPicture(background)

        drawTime(canvas, viewRect.exactCenterX(), viewRect.exactCenterY(), contentRadius)
    }

    private fun drawNumbersCircle(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val offsetY = (paint.descent() + paint.ascent()) / 2

        var num = 1
        val charWidth = paint.measureText(num.toString()) / 2
        for (angle in 60 downTo -270 step 30) {
            val text = when (numberType) {
                NumberType.ARABIC -> num.toString()
                NumberType.ROMAN -> num.toRoman()
            }

            val (dx, dy) = getShift(angle, radius)
            val offset = when {
                num == 12 -> -paint.ascent()
                num == 6 -> 0f
                angle % 180 == 0 -> -offsetY
                else -> charWidth * dy / abs(dx) - paint.ascent() / 2
            }

            canvas.drawText(text, cx + dx - charWidth * dx.sign, cy - dy + offset, paint)
            num++
        }
    }

    private fun drawDiamondCircle(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val size = (2 * PI * radius / 60).toFloat()
        val smallRadius = size * 0.1f
        val bigRadius = size * 0.33f

        for (angle in 0 until 360 step 6) {
            val (dx, dy) = getShift(angle, radius)
            if (angle % 5 == 0) {
                canvas.drawCircle(cx + dx, cy - dy, bigRadius, paint)
            } else {
                canvas.drawCircle(cx + dx, cy - dy, smallRadius, paint)
            }
        }
    }

    private fun drawTime(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        time.timeInMillis = System.currentTimeMillis()
        val hour = time.get(Calendar.HOUR_OF_DAY) % 12
        val minute = time.get(Calendar.MINUTE)
        val second = time.get(Calendar.SECOND)

        val arrows = arrayOf(
            Triple(radius * 0.55f, 90 - 30 * hour - minute / 2, hourHandColor),
            Triple(radius * 0.75f, 90 - 6 * minute - second / 10, minuteHandColor),
            Triple(radius * 0.95f, 90 - 6 * second, secondHandColor)
        )

        var size = 1
        for ((r, angle, color) in arrows) {
            val (dx, dy) = getShift(angle, r)
            arrowPaint.strokeWidth = arrowWidth / size
            arrowPaint.color = color
            canvas.drawLine(cx - dx * 0.2f, cy + dy * 0.2f, cx + dx, cy - dy, arrowPaint)
            size += 1
        }
    }



    private fun getShift(angle: Int, radius: Float, accuracy: Int = 3): Pair<Float, Float> {
        val dx = radius * cos(angle.toRadians()).roundToFloat(accuracy)
        val dy = radius * sin(angle.toRadians()).roundToFloat(accuracy)
        return dx to dy
    }

    private fun Context.dpToPx(dp: Int): Float = dp.toFloat() * this.resources.displayMetrics.density

    private fun Int.toRadians(): Double = Math.toRadians(this.toDouble())

    private fun Int.toRoman(): String = when (this) {
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        6 -> "VI"
        7 -> "VII"
        8 -> "VIII"
        9 -> "IX"
        10 -> "X"
        11 -> "XI"
        12 -> "XII"
        else -> ""
    }

    private fun Double.roundToFloat(accuracy: Int = 0): Float {
        val power = 10f.pow(accuracy)
        return (this * power).roundToInt() / power
    }

    private fun withUpdateLock(block: () -> Unit) {
        needUpdate = false
        block()
        needUpdate = true
    }

    private class SaveState : BaseSavedState, Parcelable {
        var borderWidth: Float = DEFAULT_BORDER_WIDTH.toFloat()
        var borderColor: Int = DEFAULT_COLOR
        var textColor: Int = DEFAULT_COLOR
        var diamondColor: Int = DEFAULT_COLOR
        var clockFaceColor: Int = DEFAULT_BACKGROUND_COLOR
        var secondHandColor: Int = DEFAULT_COLOR
        var minuteHandColor: Int = DEFAULT_COLOR
        var hourHandColor: Int = DEFAULT_COLOR
        var hasNumbers: Boolean = true
        var hasDiamonds: Boolean = true
        var numberType: NumberType = NumberType.ARABIC
        var timeZone: TimeZone = TimeZone.getDefault()

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel?) : super(src) {
            if (src != null) {
                borderWidth = src.readFloat()
                borderColor = src.readInt()
                textColor = src.readInt()
                diamondColor = src.readInt()
                clockFaceColor = src.readInt()
                secondHandColor = src.readInt()
                minuteHandColor = src.readInt()
                hourHandColor = src.readInt()
                hasNumbers = src.readInt() == 1
                hasDiamonds = src.readInt() == 1
                numberType = NumberType.values().getOrNull(src.readInt()) ?: NumberType.ARABIC
                timeZone = TimeZone.getTimeZone(src.readString())
            }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(borderWidth)
            out.writeInt(borderColor)
            out.writeInt(textColor)
            out.writeInt(diamondColor)
            out.writeInt(clockFaceColor)
            out.writeInt(secondHandColor)
            out.writeInt(minuteHandColor)
            out.writeInt(hourHandColor)
            out.writeInt(numberType.ordinal)
            out.writeInt(if (hasNumbers) 1 else 0)
            out.writeInt(if (hasDiamonds) 1 else 0)
            out.writeString(timeZone.id)
        }

        override fun describeContents(): Int = 0

        companion object Creator : Parcelable.Creator<SaveState> {
            override fun createFromParcel(source: Parcel?): SaveState = SaveState(source)

            override fun newArray(size: Int): Array<SaveState?> = arrayOfNulls(size)
        }
    }
}