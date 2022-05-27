package at.shehata.ex3.feature

import at.shehata.ex3.feature.geo.GeoObject
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Area as AwtArea

data class PresentationSchema(
	private val mFillColor: Color,
	private val mLineColor: Color,
	private val mLineWidth: Float = 1f
) {
	fun paint(_g: Graphics2D, _obj: GeoObject, _m: Matrix) {
		val area = AwtArea()
		_g.stroke = BasicStroke(mLineWidth)
		_g.color = mFillColor
		_obj.mObjects.forEach { it.draw(_g, _m, area) }
		_g.fill(area)
		_g.color = mLineColor
		_g.draw(area)

	}
}