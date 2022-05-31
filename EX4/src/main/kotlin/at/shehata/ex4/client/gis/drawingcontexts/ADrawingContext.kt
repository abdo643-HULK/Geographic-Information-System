package at.shehata.ex4.client.gis.drawingcontexts

import at.shehata.ex4.ui.PresentationSchema
import java.awt.Color
import java.util.Hashtable

abstract class ADrawingContext(
    protected val mContext: Hashtable<Int, PresentationSchema> = Hashtable()
) {
    init {
        initSchemata()
    }

    protected abstract fun initSchemata()

    /// Some documentation …
    fun getSchema(_type: Int) = mContext[_type]

    /// Some documentation …
    fun getDefaultSchema() = PresentationSchema(Color.BLACK, Color.WHITE)
}