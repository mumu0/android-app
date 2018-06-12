package one.mixin.android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import one.mixin.android.R

class RecordCircleView : View {

    private val colorCircle: Int by lazy { ContextCompat.getColor(context, R.color.color_record_circle_bg) }
    private val colorLock: Int by lazy { ContextCompat.getColor(context, R.color.text_gray) }
    private val colorOrange: Int by lazy { ContextCompat.getColor(context, R.color.color_blink) }

    private val paint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCircle
        }
    }

    private val paintRecord: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorOrange
        }
    }

    private val rect = RectF()
    var scale = 0f
        set(value) {
            field = value
            invalidate()
        }
    private var amplitude = 0f
    private var animateToAmplitude = 0f
    private var animateAmplitudeDiff = 0f
    private var lastUpdateTime = 0L
    var lockAnimatedTranslation = 0f
        set(value) {
            field = value
            invalidate()
        }
    var startTranslation = 0f
    var sendButtonVisible = false
    private var pressedEnd = false
    private var pressedSend = false

    lateinit var callback: Callback

    private val audioDrawable: Drawable by lazy { resources.getDrawable(R.drawable.ic_record_mic_white, null) }
    private val sendDrawable: Drawable by lazy { resources.getDrawable(R.drawable.ic_send_white_24dp, null) }

    private val lockDrawable: Drawable by lazy {
        resources.getDrawable(R.drawable.lock_middle, null).apply {
            colorFilter = PorterDuffColorFilter(colorLock, PorterDuff.Mode.MULTIPLY)
        }
    }
    private val lockTopDrawable: Drawable by lazy {
        resources.getDrawable(R.drawable.lock_top, null).apply {
            colorFilter = PorterDuffColorFilter(colorLock, PorterDuff.Mode.MULTIPLY)
        }
    }
    private val lockArrowDrawable: Drawable by lazy {
        resources.getDrawable(R.drawable.lock_arrow, null).apply {
            colorFilter = PorterDuffColorFilter(colorLock, PorterDuff.Mode.MULTIPLY)
        }
    }
    private val lockBackgroundDrawable: Drawable by lazy {
        resources.getDrawable(R.drawable.lock_round, null).apply {
            colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
        }
    }
    private val lockShadowDrawable: Drawable by lazy {
        resources.getDrawable(R.drawable.lock_round_shadow, null).apply {
            colorFilter = PorterDuffColorFilter(colorCircle, PorterDuff.Mode.MULTIPLY)
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setAmplitude(value: Double) {
        animateToAmplitude = Math.min(100.0, value).toFloat() / 100.0f
        animateAmplitudeDiff = (animateToAmplitude - amplitude) / 150.0f
        lastUpdateTime = System.currentTimeMillis()
        invalidate()
    }

    fun setSendButtonInvisible() {
        sendButtonVisible = false
        invalidate()
    }

    fun setLockTranslation(value: Float): Int {
        if (value == 10000f) {
            sendButtonVisible = false
            lockAnimatedTranslation = -1f
            startTranslation = -1f
            invalidate()
            return 0
        } else {
            if (sendButtonVisible) {
                return 2
            }
            if (lockAnimatedTranslation == -1f) {
                startTranslation = value
            }
            lockAnimatedTranslation = value
            invalidate()
            if (startTranslation - lockAnimatedTranslation >= AndroidUtilities.dp(57f)) {
                sendButtonVisible = true
                return 2
            }
        }
        return 1
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (sendButtonVisible) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            if (event.action == MotionEvent.ACTION_DOWN) {
                pressedEnd = lockBackgroundDrawable.bounds.contains(x, y)
                pressedSend = sendDrawable.bounds.contains(x, y)
                if (pressedEnd || pressedSend) {
                    return true
                }
            } else if (pressedEnd) {
                if (event.action == MotionEvent.ACTION_UP) {
                    if (lockBackgroundDrawable.bounds.contains(x, y)) {
                        callback.onCancel()
                    }
                }
                return true
            } else if (pressedSend) {
                if (event.action == MotionEvent.ACTION_UP) {
                    if (sendDrawable.bounds.contains(x, y)) {
                        callback.onSend()
                    }
                }
                return true
            }
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        val cx = measuredWidth / 2
        var cy = AndroidUtilities.dp(170f)
        var yAdd = 0f

        if (lockAnimatedTranslation != 10000f) {
            yAdd = Math.max(0f, startTranslation - lockAnimatedTranslation)
            if (yAdd > AndroidUtilities.dp(57f)) {
                yAdd = AndroidUtilities.dp(57f).toFloat()
            }
        }
        cy -= yAdd.toInt()

        val sc: Float
        val alpha: Float
        if (scale <= 0.5f) {
            sc = scale / 0.5f
            alpha = sc
        } else if (scale <= 0.75f) {
            sc = 1.0f - (scale - 0.5f) / 0.25f * 0.1f
            alpha = 1f
        } else {
            sc = 0.9f + (scale - 0.75f) / 0.25f * 0.1f
            alpha = 1f
        }
        val dt = System.currentTimeMillis() - lastUpdateTime
        if (animateToAmplitude != amplitude) {
            amplitude += animateAmplitudeDiff * dt
            if (animateAmplitudeDiff > 0) {
                if (amplitude > animateToAmplitude) {
                    amplitude = animateToAmplitude
                }
            } else {
                if (amplitude < animateToAmplitude) {
                    amplitude = animateToAmplitude
                }
            }
            invalidate()
        }
        lastUpdateTime = System.currentTimeMillis()
        if (amplitude != 0f) {
            canvas.drawCircle(measuredWidth / 2.0f, cy.toFloat(), (AndroidUtilities.dp(42f) + AndroidUtilities.dp(20f) * amplitude) * scale, paintRecord)
        }
        canvas.drawCircle(measuredWidth / 2.0f, cy.toFloat(), AndroidUtilities.dp(42f) * sc, paint)
        val drawable: Drawable = if (sendButtonVisible) {
            sendDrawable
        } else {
            audioDrawable
        }
        drawable.setBounds(cx - drawable.intrinsicWidth / 2, cy - drawable.intrinsicHeight / 2, cx + drawable.intrinsicWidth / 2, cy + drawable.intrinsicHeight / 2)
        drawable.alpha = (255 * alpha).toInt()
        drawable.draw(canvas)

        val moveProgress = 1.0f - yAdd / AndroidUtilities.dp(57f)
        val moveProgress2 = Math.max(0.0f, 1.0f - yAdd / AndroidUtilities.dp(57f) * 2)
        val lockSize: Int
        val lockY: Int
        val lockTopY: Int
        val lockMiddleY: Int
        val lockArrowY: Int
        var intAlpha = (alpha * 255).toInt()
        if (sendButtonVisible) {
            lockSize = AndroidUtilities.dp(31f)
            lockY = AndroidUtilities.dp(57f) + (AndroidUtilities.dp(30f) * (1.0f - sc) - yAdd + AndroidUtilities.dp(20f) * moveProgress).toInt()
            lockTopY = lockY + AndroidUtilities.dp(5f)
            lockMiddleY = lockY + AndroidUtilities.dp(11f)
            lockArrowY = lockY + AndroidUtilities.dp(25f)

            intAlpha *= (yAdd / AndroidUtilities.dp(57f)).toInt()
            lockBackgroundDrawable.alpha = 255
            lockShadowDrawable.alpha = 255
            lockTopDrawable.alpha = intAlpha
            lockDrawable.alpha = intAlpha
            lockArrowDrawable.alpha = (intAlpha * moveProgress2).toInt()
        } else {
            lockSize = AndroidUtilities.dp(31f) + (AndroidUtilities.dp(29f) * moveProgress).toInt()
            lockY = AndroidUtilities.dp(57f) + (AndroidUtilities.dp(30f) * (1.0f - sc)).toInt() - yAdd.toInt()
            lockTopY = lockY + AndroidUtilities.dp(5f) + (AndroidUtilities.dp(4f) * moveProgress).toInt()
            lockMiddleY = lockY + AndroidUtilities.dp(11f) + (AndroidUtilities.dp(10f) * moveProgress).toInt()
            lockArrowY = lockY + AndroidUtilities.dp(25f) + (AndroidUtilities.dp(16f) * moveProgress).toInt()

            lockBackgroundDrawable.alpha = intAlpha
            lockShadowDrawable.alpha = intAlpha
            lockTopDrawable.alpha = intAlpha
            lockDrawable.alpha = intAlpha
            lockArrowDrawable.alpha = (intAlpha * moveProgress2).toInt()
        }

        lockBackgroundDrawable.setBounds(cx - AndroidUtilities.dp(15f), lockY, cx + AndroidUtilities.dp(15f), lockY + lockSize)
        lockBackgroundDrawable.draw(canvas)
        lockShadowDrawable.setBounds(cx - AndroidUtilities.dp(16f), lockY - AndroidUtilities.dp(1f), cx + AndroidUtilities.dp(16f), lockY + lockSize + AndroidUtilities.dp(1f))
        lockShadowDrawable.draw(canvas)
        lockTopDrawable.setBounds(cx - AndroidUtilities.dp(6f), lockTopY, cx + AndroidUtilities.dp(6f), lockTopY + AndroidUtilities.dp(14f))
        lockTopDrawable.draw(canvas)
        lockDrawable.setBounds(cx - AndroidUtilities.dp(7f), lockMiddleY, cx + AndroidUtilities.dp(7f), lockMiddleY + AndroidUtilities.dp(12f))
        lockDrawable.draw(canvas)
        lockArrowDrawable.setBounds(cx - AndroidUtilities.dp(7.5f), lockArrowY, cx + AndroidUtilities.dp(7.5f), lockArrowY + AndroidUtilities.dp(9f))
        lockArrowDrawable.draw(canvas)
        if (sendButtonVisible) {
            rect.set(cx - AndroidUtilities.dp(6.5f).toFloat(), lockY + AndroidUtilities.dp(9f).toFloat(), cx + AndroidUtilities.dp(6.5f).toFloat(), lockY.toFloat() + AndroidUtilities.dp((9 + 13).toFloat()))
            canvas.drawRoundRect(rect, AndroidUtilities.dp(1f).toFloat(), AndroidUtilities.dp(1f).toFloat(), paintRecord)
        }
    }

    interface Callback {
        fun onSend()
        fun onCancel()
    }
}