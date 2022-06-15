package at.shehata.ex4.utils

import javafx.scene.canvas.GraphicsContext

/**
 * Converts an enum value without throwing an error
 *
 * @param _value The value to parse
 */
inline fun <reified T : Enum<T>> safeValueOf(_value: String): T? {
	return try {
		java.lang.Enum.valueOf(T::class.java, _value)
	} catch (_e: IllegalArgumentException) {
		return null
	}
}

/**
 * A helper to draw a Circle
 *
 * @param _ctx The context to draw on
 * @param _diagonal The size of the Circle
 */
fun drawCircle(_ctx: GraphicsContext, _diagonal: Double) {
	val (width, height) = _ctx.canvas.width to _ctx.canvas.height
	_ctx.strokeOval((width - _diagonal) / 2, (height - _diagonal) / 2, _diagonal, _diagonal)
}