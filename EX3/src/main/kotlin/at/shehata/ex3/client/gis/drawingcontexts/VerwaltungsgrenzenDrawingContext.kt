package at.shehata.ex3.client.gis.drawingcontexts

import at.shehata.ex3.feature.PresentationSchema
import java.awt.Color
import java.util.*

/**
 * The initial capacity of the hashtable to preallocate
 * correctly without needing to resize
 */
private const val CAPACITY = 1

/**
 * The drawing context for the Verwaltungsgrenzen server
 */
class VerwaltungsgrenzenDrawingContext : ADrawingContext(Hashtable(CAPACITY)) {
    override fun initSchemata() {
        mContext.apply {
            put(8002, PresentationSchema(Color.DARK_GRAY, Color.BLACK))
        }
    }
}