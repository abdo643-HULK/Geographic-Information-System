package at.shehata.ex3.utils

import java.awt.BasicStroke
import java.awt.Graphics2D
import java.awt.Point
import java.awt.geom.Area

class Line(
	private val mGeometry: List<Point> = mutableListOf()
) : GeoObjectPart() {
	override fun draw(_ctx: Graphics2D, _world: Matrix, _area: Area) {
		for (i in 0 until mGeometry.size - 1) {
			val pt1 = _world * mGeometry[i]
			val pt2 = _world * mGeometry[i + 1]
			_ctx.stroke = BasicStroke(5f)
			_ctx.drawLine(pt1.x, pt1.y, pt2.x, pt2.y)
		}
	}
}