package at.shehata.ex3.client.gis.drawingcontexts

import at.shehata.ex3.feature.PresentationSchema
import java.awt.Color
import java.util.Hashtable


/**
 * A Class to represent each server's presentation-schemas
 *
 * @param mContext
 */
abstract class ADrawingContext(
    /**
     * The table holding all the schemas
     */
    protected val mContext: Hashtable<Int, PresentationSchema> = Hashtable()
) {
    init {
        initSchemata()
    }

    /**
     * Adds all the schemas to the mContext
     */
    protected abstract fun initSchemata()

    /**
     * Gets the corresponding schema of the given type
     *
     * @param _type the type to get the schema for
     * @return A presentation schema for the type if available or the default schema
     */
    fun getSchema(_type: Int) = mContext[_type] ?: getDefaultSchema()

    /**
     * A Default schema for unknown types
     *
     * @return presentation schema
     */
    fun getDefaultSchema() = PresentationSchema(Color.BLACK, Color.WHITE)
}