package at.shehata.ex3.feature.geo.objectpart

import at.shehata.ex3.feature.Matrix
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.Area

/**
 * Class to represent a Line in a GeoObject
 */
class Line(
	/**
	 * The List of Points that get drawn as a connected line
	 */
	private val mGeometry: List<Point> = mutableListOf()
) : GeoObjectPart() {
	override val mBounds: Rectangle = Rectangle(mGeometry[0].x, mGeometry[0].y, 1, 1).apply {
		mGeometry
			.iterator()
			.apply { next() }
			.forEach { add(it.x, it.y) }
	}


	override fun draw(_ctx: Graphics2D, _world: Matrix, _area: Area) {
		/// draws a line on the Graphics 2D
		for (i in 0 until mGeometry.size - 1) {
			val pt1 = _world * mGeometry[i]
			val pt2 = _world * mGeometry[i + 1]

			_ctx.drawLine(pt1.x, pt1.y, pt2.x, pt2.y)
		}
	}
}