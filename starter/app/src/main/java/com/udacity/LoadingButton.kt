package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // custom attributes - set in init
    private var btnDefaultColor = 0
    private var btnDefaultTitle = ""
    private var btnAlternativeColor = 0
    private var btnAlternativeTitle = ""
    private var btnProgressCircleColor = 0

    // button width / height
    private var widthSize = 0
    private var heightSize = 0

    // loading animation done via second rectangle
    private var percLoadingBar: Float = 0.0f
    private var btnTitle: String = ""

    // activate / block the button animation from outside
    private var btnActive = false

    // setter function for "active state of the button" (to activate programmatically)
    fun setActive(state: Boolean) {
        btnActive = state
    }

    // setter function for the loading state (to switch off animation at the end of the download)
    fun setState(state: ButtonState) {
        buttonState = state
    }

    // initialize some basic properties to avoid having to do so in onDraw
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 60.0f
        // typeface = Typeface.create("", Typeface.BOLD)
    }

    // simple animations can be done using the ValueAnimator class
    // see: https://developer.android.com/guide/topics/graphics/prop-animation#value-animator
    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {

        // force 'emulated' progress to start over, in case we've reached 100 and the download
        // hasn't finished (see answer to https://knowledge.udacity.com/questions/489831)
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART

        // nominal time to cycle through the entire range (0.0 --> 1.0)
        duration = 1500
    }

    // ... 'by Delegates.observer()'
    // --> provided lambda is called everytime buttonState is changed
    // --> see: https://kotlinlang.org/docs/delegated-properties.html#delegating-to-another-property
    private var buttonState: ButtonState by Delegates
        .observable<ButtonState>(ButtonState.Completed) { _, old, new ->

            Timber.i("buttonState observer: state changed from ${old} to ${new}")

            // animation state machine
            when (new) {

                is ButtonState.Clicked -> {
                    // register update listener
                    valueAnimator.addUpdateListener {
                        percLoadingBar= it.animatedValue as Float
                        invalidate()
                    }

                    // start valueAnimator
                    valueAnimator.start()

                    // and immediately transition to next state
                    buttonState = buttonState.next()

                }  // ButtonState.Clicked

                is ButtonState.Loading -> {
                    // switch button title
                    btnTitle = context.getString(R.string.button_loading)

                    // update view
                    invalidate()

                }  // ButtonState.Loading

                is ButtonState.Completed -> {
                    // stop valueAnimator
                    valueAnimator.removeAllListeners()
                    valueAnimator.cancel()

                   // end of download action - run this off the main thread
                    GlobalScope.launch {

                        // quick jump to 100% - like a Munich bus...
                        percLoadingBar = 1.0f
                        invalidate()

                        // wait for it...
                        delay(300L)

                        // reset loading bar and title
                        percLoadingBar = 0.0f
                        btnTitle = btnDefaultTitle
                        invalidate()

                    }  // coroutine scope

                }  // ButtonState.Complete

            }  // when (new)

    }  // observer for ButtonState


    // dynamic initializations
    init {

        // get custom attributes
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            btnDefaultColor = getColor(R.styleable.LoadingButton_defaultColor, Color.BLUE)
            btnDefaultTitle = getString(R.styleable.LoadingButton_defaultTitle) ?: "Load"
            btnAlternativeColor = getColor(R.styleable.LoadingButton_alternativeColor, Color.LTGRAY)
            btnAlternativeTitle = getString(R.styleable.LoadingButton_alternativeTitle) ?: "Loading..."
            btnProgressCircleColor = getColor(R.styleable.LoadingButton_progressCircleColor, Color.YELLOW)
        }

        // starting with btnTitle set to btnDefaultTitle
        btnTitle = btnDefaultTitle

        // activate performClick ... plus the onClickListeners
        isClickable = true

    }  // init { ... }


    // generally, user interactions for CustomViews are controlled by performClick
    // ... leaving "onClickListener" free for "use case dependent" actions (--> here: download() )
    override fun performClick(): Boolean {
        //if (super.performClick()) return true
        super.performClick()

        // only allow the loading animation to run when it has been activated
        // (= pre-conditions have been met - this is controlled from the outside through a setter)
        if(btnActive && buttonState == ButtonState.Completed) {
            // adjust state when button is clicked and in state 'Completed' (no re-triggering)
            buttonState = buttonState.next()
        }

        return true
    }


    // screen rotated, etc. --> sizes may change
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // adjust size values of the custom view element
        widthSize = w
        heightSize = h

    }

    // draw da custom button
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // compute these only once
        val heightButton = heightSize.toFloat()
        val widthButton = widthSize.toFloat()
        val widthLoadingBar = percLoadingBar * widthButton

        // draw 'loading' rectangle
        paint.color = btnAlternativeColor
        canvas?.drawRect(0.0f, 0.0f, widthLoadingBar, heightButton, paint)

        // draw rectangle with full width of the button and primary color
        paint.color = btnDefaultColor
        canvas?.drawRect(widthLoadingBar, 0.0f, widthButton, heightButton, paint)

        // loading "progress circle"
        paint.color = btnProgressCircleColor
        val angle = (((widthLoadingBar.roundToInt() % widthSize).toFloat() / widthSize) * 360)
        canvas?.drawArc(
            widthButton * 0.75f,
            heightButton/2 - 35,
            widthButton * 0.75f + 70,
            heightButton/2 + 35,
            -90f,
            angle,
            true,
            paint
        )

        // display title (as per layout xml - custom attribute 'defaultTitle')
        paint.color = Color.WHITE
        canvas?.drawText(btnTitle, (widthSize/2).toFloat(), (heightSize/2 + 20).toFloat(), paint)

    }

    // get actual size values (depends on rotation, device, etc.)
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}