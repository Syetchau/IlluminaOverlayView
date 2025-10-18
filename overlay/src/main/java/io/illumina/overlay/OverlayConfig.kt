package io.illumina.overlay

import android.view.View

/**
 * Configuration data class for defining how each tooltip overlay item should be drawn.
 *
 * Each [OverlayConfig] instance represents a single focus area on the screen — typically
 * linked to a UI [view] you want to highlight, with descriptive text and optional
 * customization of shape, text alignment, and line connection.
 *
 * Example usage:
 * ```
 * val configs = listOf(
 *     Config(
 *         view = binding.buttonNext,
 *         text = "This is Fab",
 *         overlayAnchorPosition = Position.Top
 *     )
 * )
 * overlayView.configs = configs.toTypedArray()
 * ```
 *
 * @param view The target [View] to highlight with a transparent cutout and description text.
 * @param text String for the tooltip message that describes this view.
 * @param overlayAnchorPosition Defines the relative position of the tooltip text
 * (top, bottom, left, or end) in relation to the highlighted view.
 */
data class OverlayConfig(
    val view: View,
    val text: String,
    val overlayAnchorPosition: Position
) {
    // The location of the target view on the screen (x, y coordinates)
    private val locationOnScreen = IntArray(2).also { view.getLocationOnScreen(it) }

    // For Circle
    val centerX get() = locationOnScreen[0] + view.width / 2f
    val centerY get() = locationOnScreen[1] + view.height / 2f

    // For Rectangle
    val startX get() = locationOnScreen[0].toFloat()
    val endX get() = startX + view.width
    val startY get() = locationOnScreen[1].toFloat()
    val endY get() = startY + view.height
}

/**
 * Represents the position of the tooltip text relative to the highlighted view.
 */
enum class Position {
    /** Tooltip text appears above the highlighted view. */
    Top,

    /** Tooltip text appears below the highlighted view. */
    Bottom,

    /** Tooltip text appears to the left of the highlighted view. */
    Start,

    /** Tooltip text appears to the right (end) of the highlighted view. */
    End
}
