package at.shehata.ex3.utils

import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.geom.Area as AwtArea

class Area(
	@get:JvmName("getGeometry")
	val mGeometry: Polygon = Polygon(),
	override val mHoles: List<GeoObjectPart> = mutableListOf()
) : GeoObjectPart() {
	override fun draw(_ctx: Graphics2D, _world: Matrix, _area: AwtArea) {
		_area.add(AwtArea(_world * mGeometry))
		mHoles.forEach {
			_area.subtract(AwtArea(_world * (it as Area).mGeometry))
		}
	}
}