package at.shehata.ex3.client.gis

import at.shehata.ex3.utils.GeoObject
import at.shehata.ex3.utils.Matrix
import java.awt.BasicStroke
import java.awt.Graphics2D
import java.awt.Color

data class PresentationSchema(
    private val mFillColor: Color,
    private val mLineColor: Color,
    private val mLineWidth: Float = 1.0f
) {
    fun paint(_g: Graphics2D, _obj: GeoObject, _m: Matrix) {
        val resizedPolys = _obj.mPolygons.map { _m * it }
        _g.color = mFillColor
        resizedPolys.forEach { _g.fillPolygon(it) }
        _g.color = mLineColor
        _g.stroke = BasicStroke(mLineWidth)
        resizedPolys.forEach { _g.drawPolygon(it) }
    }
}