package at.shehata.ex3.gis

import java.awt.Color
import java.util.*

const val CAPACITY = 4

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