package at.shehata.ex4.utils

import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Area

class Point(
	@get:JvmName("getGeometry")
	val mGeometry: java.awt.Point
) : GeoObjectPart() {
	override fun draw(_ctx: Graphics2D, _world: Matrix, _area: Area) {
		_area.add(
			Area(_world * Rectangle(mGeometry.x, mGeometry.y, 1, 1))
		)
	}
}