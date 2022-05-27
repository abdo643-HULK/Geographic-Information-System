package at.shehata.ex3.feature.geo

import at.shehata.ex3.feature.geo.objectpart.GeoObjectPart
import java.awt.Rectangle

/**
 * Class for the Data from the Server
 */
open class GeoObject(
	/**
	 * Liefert die Id des Geo-Objektes
	 *
	 * @return Die Id des Objektes
	 * @see kotlin.String
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
	val mBounds: Rectangle by lazy {
		Rectangle(mObjects[0].mBounds).apply {
			mObjects
				.iterator()
				.apply { next() }
				.forEach { add(it.mBounds) }
		}
	}

	override fun toString(): String =
		"GeoObject(mId=$mId,mType=$mType,mObject=${mObjects.toTypedArray().contentToString()})"
}