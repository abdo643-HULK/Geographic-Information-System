package at.shehata.ex3.feature.geo.objectpart

import at.shehata.ex3.feature.Matrix
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Area

/**
 * Class to represent a Point in a GeoObject
 */
class Point(
	/**
	 * The Point that gets drawn
	 */
	@get:JvmName("getGeometry")
	val mGeometry: java.awt.Point
) : GeoObjectPart() {
	override val mBounds: Rectangle = Rectangle(mGeometry.x, mGeometry.y, 1, 1)

	override fun draw(_ctx: Graphics2D, _world: Matrix, _area: Area) {
		/// creates a Rectangle and adds it to the area
		_area.add(
			Area(_world * Rectangle(mGeometry.x, mGeometry.y, 1, 1))
		)
	}
}