package at.shehata.ex4.nmea.messages

import at.shehata.ex4.nmea.SatType
import at.shehata.ex4.nmea.interfaces.Message
import java.nio.CharBuffer

/**
 * Object that parses and holds the data of a GGA message
 *
 * @param mTime UTC-Time
 * @param mLatitude The Latitude in Decimal-degree
 * @param mLatDir The direction of the Latitude either N or S
 * @param mLongitude The Longitude in Decimal-degree
 * @param mLongDir The direction of the Latitude either E or W
 * @param mQuality Fix-Quality (0 – no fix, 1 – GPS fix, 2 – Differential GPS fix)
 * @param mSatelliteCount Number of Satellites used to calculate the position
 * @param mHDOP Horizontal Dilution of precision
 * @param mAltitude height over the Geoid
 * @param mAltitudeUnit Used unit for the mAltitude
 * @param mDifference difference between Ellipsoid height and Geoid height
 * @param mDifferenceUnit Used unit for the mDifference
 * @param mAge Age of the DGPS Information
 * @param mId Id of the DGPS Station
 */
data class GGA(
	val mTime: CharBuffer?,
	val mLatitude: CharBuffer?,
	val mLatDir: LatDirection?,
	val mLongitude: CharBuffer?,
	val mLongDir: LongDirection?,
	val mQuality: FixQuality?,
	val mSatelliteCount: UInt?,
	val mHDOP: Float?,
	val mAltitude: Float?,
	val mAltitudeUnit: CharBuffer?,
	val mDifference: Float?,
	val mDifferenceUnit: CharBuffer?,
	val mAge: Float?,
	val mId: Int?
) : Message {

	/**
	 * The possible Directions for the Latitude
	 */
	enum class LatDirection {
		N, S
	}

	/**
	 * The possible Directions for the Longitude
	 */
	enum class LongDirection {
		E, W
	}

	/**
	 * The possible Values for the Fix-Quality
	 */
	enum class FixQuality(val mVal: UInt) {
		NO_FIX(0u),
		GPS_FIX(1u),
		DIFFERENTIAL_GPS_FIX(2u);

		companion object {
			/**
			 * creates an enum from UInt
			 *
			 * @param _s the value to convert
			 */
			fun from(_s: UInt): FixQuality? = values().find { it.mVal == _s }
		}
	}

	companion object {
		/**
		 * Takes a GGA sentence and returns a GGA Object
		 *
		 * @param _buffer the sentence to parse
		 * @param _satType is there for compatibility
		 *
		 * @return The parsed GSA Message
		 */
		fun handle(_buffer: CharBuffer, _satType: SatType): GGA {
			val data = arrayOfNulls<CharBuffer>(14)

			var i = 0
			var delimiter: Int
			while (_buffer
					.indexOf(',')
					.also { delimiter = it } != -1
			) {
				data[i++] = _buffer
					.slice(_buffer.position(), delimiter)
					.ifEmpty { null }
				_buffer.position(_buffer.position() + delimiter + 1)
			}

			delimiter = _buffer.indexOf('*')
			data[i] = _buffer
				.slice(_buffer.position(), delimiter)
				.ifEmpty { null }
			_buffer.position(_buffer.position() + delimiter + 1)

			return GGA(
				mTime = data[0],
				mLatitude = data[1],
				mLatDir = data[2]?.let { LatDirection.valueOf(it.toString()) },
				mLongitude = data[3],
				mLongDir = data[4]?.let { LongDirection.valueOf(it.toString()) },
				mQuality = data[5]?.let {
					FixQuality.from(
						it
							.toString()
							.toUInt()
					)
				},
				mSatelliteCount = data[6]
					?.toString()
					?.toUInt(),
				mHDOP = data[7]
					?.toString()
					?.toFloat(),
				mAltitude = data[8]
					?.toString()
					?.toFloat(),
				mAltitudeUnit = data[9],
				mDifference = data[10]
					?.toString()
					?.toFloat(),
				mDifferenceUnit = data[11],
				mAge = data[12]
					?.toString()
					?.toFloat(),
				mId = data[13]
					?.toString()
					?.toInt(),
			)
		}
	}
}