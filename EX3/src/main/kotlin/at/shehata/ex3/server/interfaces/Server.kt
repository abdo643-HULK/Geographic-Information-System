package at.shehata.ex3.server.interfaces

import at.shehata.ex3.client.gis.drawingcontexts.ADrawingContext
import at.shehata.ex3.utils.GeoObject
import java.awt.Rectangle

interface Server {
	val mDrawingContext: ADrawingContext
	fun loadData(): List<GeoObject>
	fun getArea(_boundingBox: Rectangle): List<GeoObject>
}