package at.shehata.ex3.client.gis.drawingcontexts

import at.shehata.ex3.client.gis.PresentationSchema
import java.awt.Color
import java.util.*


private const val CAPACITY = 1

class VerwaltungsgrenzenDrawingContext : ADrawingContext(Hashtable(CAPACITY)) {
    override fun initSchemata() {
        mContext.apply {
            put(8002, PresentationSchema(Color.WHITE, Color.BLACK))
        }
    }
}