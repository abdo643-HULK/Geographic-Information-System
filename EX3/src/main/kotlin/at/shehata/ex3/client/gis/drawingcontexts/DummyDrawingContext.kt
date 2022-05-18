package at.shehata.ex3.client.gis.drawingcontexts

import at.shehata.ex3.client.gis.PresentationSchema
import java.awt.Color
import java.util.*

private const val CAPACITY = 4

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