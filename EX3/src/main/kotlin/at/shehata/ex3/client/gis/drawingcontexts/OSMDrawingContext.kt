package at.shehata.ex3.client.gis.drawingcontexts

import at.shehata.ex3.client.gis.PresentationSchema
import java.awt.Color
import java.util.*

private const val CAPACITY = 38

class OSMDrawingContext : ADrawingContext(Hashtable(CAPACITY)) {
    override fun initSchemata() {
        mContext.apply {
            put(5001, PresentationSchema(Color.LIGHT_GRAY, Color.BLACK))
            put(5002, PresentationSchema(Color(149, 69, 53), Color.BLACK))
            put(5003, PresentationSchema(Color.CYAN, Color.BLACK))
            put(5004, PresentationSchema(Color(1, 68, 33), Color.BLACK))
            put(5005, PresentationSchema(Color(124, 252, 0), Color.BLACK))
            put(5006, PresentationSchema(Color(95, 101, 75), Color.BLACK))

            put(6001, PresentationSchema(Color.GREEN, Color.BLACK))
            put(6002, PresentationSchema(Color(0, 100, 0), Color.BLACK))
            put(6005, PresentationSchema(Color.BLUE, Color.BLACK))

            put(9099, PresentationSchema(Color.DARK_GRAY, Color.BLACK))
            for (i in 9000..9028) {
                put(i, PresentationSchema(Color(255, 200, i - 8980), Color.BLACK))
            }
        }
    }
}