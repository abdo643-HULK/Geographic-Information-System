package at.shehata.ex4.utils

import javafx.scene.canvas.GraphicsContext


inline fun <reified T : Enum<T>> safeValueOf(type: String): T? {
	return try {
		java.lang.Enum.valueOf(T::class.java, type)
	} catch (_e: IllegalArgumentException) {
		return null
	}
}


fun drawCircle(_ctx: GraphicsContext, _diagonal: Double) {
	val (width, height) = _ctx.canvas.width to _ctx.canvas.height
	_ctx.strokeOval((width - _diagonal) / 2, (height - _diagonal) / 2, _diagonal, _diagonal)
}