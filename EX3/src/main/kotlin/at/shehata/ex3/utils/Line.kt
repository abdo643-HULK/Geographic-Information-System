package at.shehata.ex3.utils

import java.awt.Graphics2D
import java.awt.Point
import java.awt.geom.Area
import java.awt.geom.Path2D

class Line(
	private val mGeometry: List<Point> = mutableListOf()
) : GeoObjectPart() {
	override fun draw(_ctx: Graphics2D, _world: Matrix, _area: Area) {
		val line = Path2D.Double()
		mGeometry.forEach {
			val point = _world * it
			line.lineTo(point.x.toDouble(), point.y.toDouble())
		}
		_area.add(Area(line))
	}
}