package at.shehata.ex3.client.gis.drawingcontexts

import at.shehata.ex3.feature.PresentationSchema
import java.awt.Color
import java.util.*

/**
 * The initial capacity of the hashtable to preallocate
 * correctly without needing to resize
 */
private const val CAPACITY = 4

/**
 * The drawing context for the DummyGIS server
 */
class DummyDrawingContext : ADrawingContext(Hashtable(CAPACITY)) {
    override fun initSchemata() {
        mContext.apply {
            put(233, PresentationSchema(Color.WHITE, Color.BLACK))
            put(931, PresentationSchema(Color.RED, Color.BLACK))
            put(932, PresentationSchema(Color.ORANGE, Color.RED))
            put(1101, PresentationSchema(Color.MAGENTA, Color.GREEN))
        }
    }
}