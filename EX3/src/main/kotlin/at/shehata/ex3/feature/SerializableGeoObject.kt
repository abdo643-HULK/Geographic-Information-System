package at.shehata.ex3.feature

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.awt.Polygon

@Serializable
private data class Poly(val mXpoints: IntArray, val mYpoints: IntArray, val mNpoints: Int)

/**
 * The overridden Function parameter names can't be renamed because of kotlin named arguments
 */
private object PolygonSerializer : KSerializer<Polygon> {
	private val delegateSerializer = Poly.serializer()

	@OptIn(ExperimentalSerializationApi::class)
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
 * Class used to create/parse the JSON created from the DummyGIS data
 */
@Serializable
data class SerializableGeoObject(
	/**
	 * Liefert die Id des Geo-Objektes
	 *
	 * @return Die Id des Objektes
	 * @see java.lang.String
	 */
	@get:JvmName("getId")
	val mId: String,
	/**
	 * Liefert den Typ des Geo-Objektes
	 *
	 * @return Der Typ des Objektes
	 */
	@get:JvmName("getType")
	val mType: Int,

	/**
	 * Liefert die Geometrie des Geo-Objektes
	 *
	 * @return das Polygon des Objektes
	 */
	@get:JvmName("getPoly")
	@Serializable(with = PolygonSerializer::class)
	val mPoly: Polygon,
)
