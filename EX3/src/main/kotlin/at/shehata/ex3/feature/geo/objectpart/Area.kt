package at.shehata.ex3.feature.geo.objectpart

import at.shehata.ex3.feature.Matrix
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.geom.Area as AwtArea

/**
 * Class to represent Multi-Polygon and Polygons
 * in a GeoObject
 */
class Area(
	/**
	 * The Polygon that represents the Area to draw
	 */
	private val mGeometry: Polygon = Polygon(),
	override val mHoles: List<GeoObjectPart> = mutableListOf(),
) : GeoObjectPart() {
	override val mBounds: Rectangle by lazy { mGeometry.bounds }

	override fun draw(_ctx: Graphics2D, _world: Matrix, _area: AwtArea) {
		/// adds the multi-polygon by creating an area and subtracting the holes
		_area.add(
			AwtArea(_world * mGeometry).apply {
				mHoles.forEach {
					subtract(AwtArea(_world * (it as Area).mGeometry))
				}
			}
		)
	}
}