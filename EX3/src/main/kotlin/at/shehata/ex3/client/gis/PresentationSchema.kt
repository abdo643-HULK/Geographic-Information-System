package at.shehata.ex3.client.gis

import at.shehata.ex3.utils.GeoObject
import at.shehata.ex3.utils.Matrix
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Area as AwtArea

data class PresentationSchema(
	private val mFillColor: Color,
	private val mLineColor: Color,
	private val mLineWidth: Float = 1.0f
) {
	fun paint(_g: Graphics2D, _obj: GeoObject, _m: Matrix) {
		_g.color = mFillColor
		val area = AwtArea()
		_obj.mObjects.forEach { it.draw(_g, _m, area) }
		_g.fill(area)
		_g.color = mLineColor
		_g.stroke = BasicStroke(mLineWidth)
		_g.draw(area)
	}
}