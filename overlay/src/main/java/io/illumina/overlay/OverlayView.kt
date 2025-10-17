package io.illumina.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import kotlin.math.max
import kotlin.math.min

/**
 * A customizable overlay view for highlighting specific UI elements with tooltips.
 *
 * ✨ Features:
 * - Highlights any target View with circle or rounded rectangle
 * - Adds an informative tooltip text connected via a line
 * - Ensures text always stays on-screen
 * - Highly configurable (colors, spacing, radius, etc.)
 */
class OverlayView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // --- Overlay configuration(s) for multiple highlights ---
    lateinit var configs: Array<OverlayConfig>

    // --- Customizable attributes with defaults ---
    var overlayBackgroundColor: Int = ContextCompat.getColor(context!!, R.color.overlay_black)
    var overlayLineColor: Int = Color.WHITE
    var overlayTextColor: Int = Color.WHITE
    var overlayTextSize: Float = resources.getDimension(R.dimen.txt_size_14)
    var overlayCornerRadius: Float = resources.getDimension(R.dimen.corner_radius_0)
    var overlayLineWidth: Float = resources.getDimension(R.dimen.width_line_tooltips)
    var overlayLineSpacingWithText: Float = resources.getDimension(R.dimen.spacing_8)
    var overlayLineLength: Float = resources.getDimension(R.dimen.length_line_tooltips)
    var highlightRadius: Float = resources.getDimension(R.dimen.highlight_radius)
    var highlightColor: Int = Color.WHITE
    var marginStartFromOverlayLine: Float = 0f
    var marginEndFromOverlayLine: Float = 0f

    // --- Paint objects for drawing different elements ---
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    // --- Bitmap + Canvas layer used to apply CLEAR mode for highlight hole ---
    private var bitmap: Bitmap? = null
    private var layer: Canvas? = null
    private val rect = RectF()

    init {
        // Load attributes from XML if any
        context?.theme?.obtainStyledAttributes(attrs, R.styleable.OverlayView, defStyleAttr, 0)?.apply {
            overlayBackgroundColor = getColor(R.styleable.OverlayView_overlayBackgroundColor, overlayBackgroundColor)
            overlayLineColor = getColor(R.styleable.OverlayView_overlayLineColor, overlayLineColor)
            overlayLineWidth = getDimension(R.styleable.OverlayView_overlayLineWidth, overlayLineWidth)
            overlayLineLength = getDimension(R.styleable.OverlayView_overlayLineLength, overlayLineLength)
            overlayLineSpacingWithText = getDimension(R.styleable.OverlayView_overlayLineSpacingWithText, overlayLineSpacingWithText)
            overlayTextColor = getColor(R.styleable.OverlayView_overlayTextColor, overlayTextColor)
            overlayTextSize = getDimension(R.styleable.OverlayView_overlayTextSize, overlayTextSize)
            overlayCornerRadius = getDimension(R.styleable.OverlayView_overlayCornerRadius, overlayCornerRadius)
            highlightRadius = getDimension(R.styleable.OverlayView_highlightRadius, highlightRadius)
            highlightColor = getColor(R.styleable.OverlayView_highlightColor, highlightColor)
            marginStartFromOverlayLine = getDimension(R.styleable.OverlayView_marginStartFromOverlayLine, 0f)
            marginEndFromOverlayLine = getDimension(R.styleable.OverlayView_marginEndFromOverlayLine, 0f)
            recycle()
        }

        // --- Initialize paint properties ---
        bgPaint.color = overlayBackgroundColor
        highlightPaint.color = highlightColor
        clearPaint.apply {
            color = Color.TRANSPARENT
            strokeWidth = overlayLineWidth
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) // clears highlight area
        }
        linePaint.apply {
            color = overlayLineColor
            strokeWidth = overlayLineWidth
        }
        textPaint.apply {
            color = overlayTextColor
            textSize = overlayTextSize
            textAlign = Paint.Align.LEFT
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // --- Initialize offscreen bitmap for clearing highlight area ---
        if (bitmap == null) configureBitmap()
        val layer = this.layer ?: return

        // --- Fill background with dim color ---
        layer.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val density = resources.displayMetrics.density
        val margin = 24f * density // margin to keep text visible

        // --- Draw overlays for each config (multi-highlight support) ---
        for (config in configs) {
            with(config) {
                // --- Step 1: Compute basic dimensions ---
                val viewWidth = view.width.toFloat()
                val viewHeight = view.height.toFloat()
                val radius = min(viewWidth, viewHeight) / 2f // ✅ Fixed: Use smaller side as circle radius
                val lineLength = overlayLineLength.toInt()
                val outerRadius = radius + highlightRadius

                // --- Step 2: Initialize line start/end coordinates ---
                var lineStartX = centerX
                var lineStartY = centerY
                var lineEndX = centerX
                var lineEndY = centerY

                // --- Step 3: Adjust for margin offsets (Start/End/Top/Bottom) ---
                when (overlayAnchorPosition) {
                    Position.Top, Position.Bottom -> {
                        val lineOffset = if (marginStartFromOverlayLine != 0f)
                            -marginStartFromOverlayLine else marginEndFromOverlayLine
                        lineStartX += lineOffset
                        lineEndX += lineOffset
                    }
                    Position.Start, Position.End -> {
                        val lineOffset = if (marginStartFromOverlayLine != 0f)
                            -marginStartFromOverlayLine else marginEndFromOverlayLine
                        lineStartY += lineOffset
                        lineEndY += lineOffset
                    }
                }

                // --- Step 4: Compute actual line direction based on anchor position ---
                when (overlayAnchorPosition) {
                    Position.Top -> {
                        lineStartY = centerY - outerRadius
                        lineEndY = lineStartY - lineLength
                    }
                    Position.Bottom -> {
                        lineStartY = centerY + outerRadius
                        lineEndY = lineStartY + lineLength
                    }
                    Position.Start -> {
                        lineStartX = centerX - outerRadius
                        lineEndX = lineStartX - lineLength
                    }
                    Position.End -> {
                        lineStartX = centerX + outerRadius
                        lineEndX = lineStartX + lineLength
                    }
                }

                // --- Step 5: Draw highlight (Circle or Rounded Rect) ---
                val isCircle = (viewWidth == viewHeight && overlayCornerRadius >= radius)
                if (isCircle) {
                    if (highlightRadius > 0f)
                        layer.drawCircle(centerX, centerY, radius + highlightRadius, highlightPaint)
                    layer.drawCircle(centerX, centerY, radius, clearPaint) // clear hole
                } else {
                    if (highlightRadius > 0f) {
                        val expandedRect = RectF(
                            startX - highlightRadius,
                            startY - highlightRadius,
                            endX + highlightRadius,
                            endY + highlightRadius
                        )
                        layer.drawRoundRect(expandedRect, overlayCornerRadius, overlayCornerRadius, highlightPaint)
                    }
                    rect.set(startX, startY, endX, endY)
                    layer.drawRoundRect(rect, overlayCornerRadius, overlayCornerRadius, clearPaint)
                }

                // --- Step 6: Draw connector line ---
                layer.drawLine(lineStartX, lineStartY, lineEndX, lineEndY, linePaint)

                // --- Step 7: Measure text width ---
                val measuredTextWidth = textPaint.measureText(text) // ✅ Keep for reference
                val screenMaxTextWidth = width - margin * 2

                // --- Step 8: Dynamically compute available width for text ---
                val dynamicMaxTextWidth = when (overlayAnchorPosition) {
                    Position.Start -> {
                        val available = lineEndX - overlayLineSpacingWithText - margin
                        max(1f, available).coerceAtMost(screenMaxTextWidth)
                    }
                    Position.End -> {
                        val available = width - margin - (lineEndX + overlayLineSpacingWithText)
                        max(1f, available).coerceAtMost(screenMaxTextWidth)
                    }
                    else -> screenMaxTextWidth
                }

                // --- Step 9: Choose text layout width ---
                val finalLayoutWidth = min(measuredTextWidth, dynamicMaxTextWidth).toInt()

                // --- Step 10: Build text layout ---
                val layout = StaticLayout.Builder
                    .obtain(text, 0, text.length, textPaint, finalLayoutWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setIncludePad(false)
                    .build()

                // --- Step 11: Compute actual text size ---
                val renderedTextWidth =
                    (0 until layout.lineCount).maxOfOrNull { layout.getLineWidth(it) } ?: finalLayoutWidth.toFloat()
                val textHeight = layout.height.toFloat()

                // --- Step 12: Position text relative to line ---
                var textStartX: Float
                var textStartY: Float

                when (overlayAnchorPosition) {
                    Position.Top -> {
                        textStartX = lineEndX - renderedTextWidth / 2f
                        textStartY = lineEndY - textHeight - overlayLineSpacingWithText
                    }
                    Position.Bottom -> {
                        textStartX = lineEndX - renderedTextWidth / 2f
                        textStartY = lineEndY + overlayLineSpacingWithText
                    }
                    Position.Start -> {
                        textStartX = lineEndX - overlayLineSpacingWithText - renderedTextWidth
                        textStartY = lineEndY - textHeight / 2f
                    }
                    Position.End -> {
                        textStartX = lineEndX + overlayLineSpacingWithText
                        textStartY = lineEndY - textHeight / 2f
                    }
                }

                // --- Step 13: Keep text fully on screen ---
                if (textStartX < margin) textStartX = margin
                if (textStartX + renderedTextWidth > width - margin)
                    textStartX = width - margin - renderedTextWidth
                if (textStartY < margin) textStartY = margin
                if (textStartY + textHeight > height - margin)
                    textStartY = height - margin - textHeight

                // --- Step 14: Draw text on layer ---
                layer.withSave {
                    translate(textStartX, textStartY)
                    layout.draw(this)
                }
            }
        }

        // --- Step 15: Draw the final composed overlay bitmap ---
        canvas.drawBitmap(bitmap!!, 0f, 0f, bgPaint)
    }

    /** Creates the bitmap + canvas for overlay rendering */
    private fun configureBitmap() {
        bitmap = createBitmap(width, height)
        layer = Canvas(bitmap!!)
    }
}
