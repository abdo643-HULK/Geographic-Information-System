package at.shehata.ex3.feature.drawingcontexts

import at.shehata.ex3.feature.PresentationSchema
import java.awt.Color
import java.util.*

/**
 * The initial capacity of the hashtable to preallocate
 * correctly without needing to resize
 */
private const val CAPACITY = 40

/**
 * Highway Types
 */
private enum class Highway(val mValue: Int) {
	MOTORWAY(1010),
	PRIMARY(1030)
}

/**
 * Landuse Types
 */
private enum class Landuse(val mValue: Int) {
	RESIDENTIAL(5001),
	INDUSTRIAL(5002),
	COMMERCIAL(5003),
	FOREST(5004),
	GRASS(5005),
	MEADOW(5006)
}

/**
 * Natural Types
 */
private enum class Natural(val mValue: Int) {
	GRASSLAND(6001),
	WOOD(6002),
	WATER(6005),
}

/**
 * The drawing context for the OSM server
 */
class OSMDrawingContext : ADrawingContext(Hashtable(CAPACITY)) {
	override fun initSchemata() {
		mContext.apply {
			put(Highway.MOTORWAY.mValue, PresentationSchema(Color.BLACK, Color.LIGHT_GRAY, 3f))
			put(Highway.PRIMARY.mValue, PresentationSchema(Color.LIGHT_GRAY, Color(149, 69, 53), 3f))

			put(Landuse.RESIDENTIAL.mValue, PresentationSchema(Color.LIGHT_GRAY, Color.BLACK))
			put(Landuse.INDUSTRIAL.mValue, PresentationSchema(Color(124, 118, 118), Color.BLACK))
			put(Landuse.COMMERCIAL.mValue, PresentationSchema(Color.CYAN, Color.BLACK))
			put(Landuse.FOREST.mValue, PresentationSchema(Color(1, 68, 33), Color.BLACK))
			put(Landuse.GRASS.mValue, PresentationSchema(Color(124, 252, 0), Color.BLACK))
			put(Landuse.MEADOW.mValue, PresentationSchema(Color(95, 101, 75), Color.BLACK))

			put(Natural.GRASSLAND.mValue, PresentationSchema(Color.GREEN, Color.BLACK))
			put(Natural.WOOD.mValue, PresentationSchema(Color(0, 100, 0), Color.BLACK))
			put(Natural.WATER.mValue, PresentationSchema(Color.BLUE, Color.BLACK))

			put(1, PresentationSchema(Color(62, 105, 190), Color.BLACK))

			val waterwayStart = 2001
			for (i in waterwayStart..2007) {
				put(i, PresentationSchema(Color(62, 105, i - waterwayStart + 190), Color.BLACK))
			}

			for (i in 9000..9028) {
				put(i, PresentationSchema(Color(255, 200, i - 8980), Color.BLACK))
			}

			put(9099, PresentationSchema(Color.DARK_GRAY, Color.BLACK))
		}
	}
}