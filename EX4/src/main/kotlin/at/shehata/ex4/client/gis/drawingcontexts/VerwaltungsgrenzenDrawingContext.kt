package at.shehata.ex4.client.gis.drawingcontexts

import at.shehata.ex4.ui.PresentationSchema
import java.awt.Color
import java.util.*


private const val CAPACITY = 1

class VerwaltungsgrenzenDrawingContext : ADrawingContext(Hashtable(CAPACITY)) {
    override fun initSchemata() {
        mContext.apply {
            put(8002, PresentationSchema(Color.DARK_GRAY, Color.BLACK))
        }
    }
}