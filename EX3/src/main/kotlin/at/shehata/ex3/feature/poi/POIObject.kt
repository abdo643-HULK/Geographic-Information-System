package at.shehata.ex3.feature.poi

import at.shehata.ex3.feature.geo.GeoObject
import at.shehata.ex3.feature.geo.objectpart.Point
import java.awt.image.BufferedImage

/**
 * The different POI types available
 */
enum class POITypes(val mType: Int) {
	MOSQUE(0),
	SCHOOL(1),
	POST(2),
	SHOP(3),
	PUB(4)
}

/**
 * The Class for POI
 */
class POIObject(
	val mImage: BufferedImage,
	_id: String,
	_type: POITypes,
	_point: Point
) : GeoObject(_id, _type.mType, listOf(_point))