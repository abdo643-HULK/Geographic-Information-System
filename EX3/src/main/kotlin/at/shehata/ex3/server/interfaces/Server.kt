package at.shehata.ex3.server.interfaces

import at.shehata.ex3.client.gis.drawingcontexts.ADrawingContext
import at.shehata.ex3.feature.geo.GeoObject
import java.awt.Rectangle

/**
 * Interface for all Server to switch between them
 */
interface Server {
	/**
	 * the drawing context of the Server
	 */
	@Suppress("INAPPLICABLE_JVM_NAME")
	@get:JvmName("getDrawingContext")
	val mDrawingContext: ADrawingContext

	/**
	 * Calls the DB to get the Objects
	 *
	 * @return the list of Objects
	 */
	fun loadData(): List<GeoObject>

	/**
	 * Returns only the area that is visible in
	 * the bounding box
	 *
	 * @param _boundingBox The Area to clip from
	 * @return list of Objects in the defined bounding box
	 */
	fun getArea(_boundingBox: Rectangle): List<GeoObject>
}