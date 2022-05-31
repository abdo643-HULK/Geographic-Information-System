package at.shehata.ex4.utils

import java.awt.Rectangle

/**
 * Class for the Data from the Server
 */
open class GeoObject(
	/**
	 * Liefert die Id des Geo-Objektes
	 *
	 * @return Die Id des Objektes
	 * @see java.lang.String
	 */
	@get:JvmName("getId")
	val mId: String,

	/**
	 * Liefert den Typ des Geo-Objektes
	 *
	 * @return Der Typ des Objektes
	 */
	@get:JvmName("getType")
	val mType: Int,

	@get:JvmName("getObject")
	val mObjects: List<GeoObjectPart>,
) {
	/**
	 * Liefert die Bounding Box der Geometrie
	 *
	 * @return die Bounding Box der Geometrie als Rechteckobjekt
	 * @see java.awt.Rectangle
	 */
	@get:JvmName("getBounds")
	val mBounds: List<Rectangle>
		@Throws(Error::class)
		get() = mObjects.mapNotNull {
			when (it) {
				is Area -> it.mGeometry.bounds
				is Line -> Rectangle(it.mGeometry[0].x, it.mGeometry[0].y, 1, 1).apply {
					it.mGeometry.forEach { pt -> add(pt.x, pt.y) }
				}
				is Point -> Rectangle(it.mGeometry.x, it.mGeometry.y, 1, 1)
				else -> null
			}
		}

	override fun toString(): String =
		"GeoObject(mId=$mId,mType=$mType,mObject=${mObjects.toTypedArray().contentToString()})"
}