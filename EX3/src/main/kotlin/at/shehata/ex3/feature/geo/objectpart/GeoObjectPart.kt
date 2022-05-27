package at.shehata.ex3.feature.geo.objectpart

import at.shehata.ex3.feature.Matrix
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Area as AwtArea

/**
 * Class for different Geometry objects
 * to inherit from
 */
abstract class GeoObjectPart(
	/**
	 * The holes in an object that are Multi-Polygons
	 */
	protected open val mHoles: List<GeoObjectPart> = mutableListOf()
) {
	/**
	 * The bounds of the object representing an object part
	 */
	@Suppress("INAPPLICABLE_JVM_NAME")
	@get:JvmName("getBounds")
	abstract val mBounds: Rectangle

	/**
	 * It either adds to the area or draws on to the
	 * graphics context
	 *
	 * @param _ctx graphics context of the image
	 * @param _world the transformation matrix of the world/screen
	 * @param _area The area that gets drawn onto the context
	 */
	abstract fun draw(_ctx: Graphics2D, _world: Matrix, _area: AwtArea)
}