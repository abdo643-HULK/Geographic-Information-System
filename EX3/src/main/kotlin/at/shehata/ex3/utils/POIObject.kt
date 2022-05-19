package at.shehata.ex3.utils

enum class POITypes(val mType: Int) {
	MOSQUE(0),
	SCHOOL(1),
	POST(2),
	SHOP(3),
	PUB(4)
}

class POIObject(
	_id: String,
	_type: POITypes,
	_point: Point
) : GeoObject(_id, _type.mType, listOf(_point))