package at.shehata.ex3.utils

import java.awt.Polygon
import java.awt.Rectangle

/**
 * Class for the Data from the Server
 */
data class GeoObject(private val mId: String, private val mType: Int, private val mPoly: Polygon) {
    /**
     * Liefert die Id des Geo-Objektes
     * @return Die Id des Objektes
     * @see java.lang.String
     */
    fun getId(): String = mId

    /**
     * Liefert den Typ des Geo-Objektes
     * @return Der Typ des Objektes
     */
    fun getType(): Int = mType

    /**
     * Liefert die Geometrie des Geo-Objektes
     * @return das Polygon des Objektes
     */
    fun getPoly(): Polygon = mPoly

    /**
     * Liefert die Bounding Box der Geometrie
     * @return die Boundin Box der Geometrie als Rechteckobjekt
     * @see java.awt.Rectangle
     */
    fun getBounds(): Rectangle = mPoly.bounds
}