package at.shehata.ex3.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.awt.Polygon
import java.awt.Rectangle

@Serializable
private data class Poly(val mXpoints: IntArray, val mYpoints: IntArray, val mNpoints: Int)

private object PolygonSerializer : KSerializer<Polygon> {
    private val delegateSerializer = Poly.serializer()
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("polygon", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: Polygon) {
        encoder.encodeSerializableValue(Poly.serializer(), Poly(value.xpoints, value.ypoints, value.npoints))
    }


    override fun deserialize(decoder: Decoder): Polygon {
        val surrogate = decoder.decodeSerializableValue(Poly.serializer())
        return Polygon(surrogate.mXpoints, surrogate.mYpoints, surrogate.mNpoints)
    }
}

/**
 * Class for the Data from the Server
 */
@Serializable
open class GeoObject(
    @get:JvmName("getId")
    val mId: String,
    @get:JvmName("getType")
    val mType: Int,
    @get:JvmName("getPoly")
    @Serializable(with = PolygonSerializer::class)
    val mPoly: Polygon,
    val mPolygons: List<@Serializable(with = PolygonSerializer::class) Polygon>
) {
    /**
     * Liefert die Id des Geo-Objektes
     * @return Die Id des Objektes
     * @see java.lang.String
     */
//    fun getId(): String = mId


    /**
     * Liefert den Typ des Geo-Objektes
     * @return Der Typ des Objektes
     */
//    fun getType(): Int = mType

    /**
     * Liefert die Geometrie des Geo-Objektes
     * @return das Polygon des Objektes
     */
//    fun getPoly(): Polygon = mPoly

    /**
     * Liefert die Bounding Box der Geometrie
     * @return die Boundin Box der Geometrie als Rechteckobjekt
     * @see java.awt.Rectangle
     */
    fun getBounds(): Rectangle = mPoly.bounds
}