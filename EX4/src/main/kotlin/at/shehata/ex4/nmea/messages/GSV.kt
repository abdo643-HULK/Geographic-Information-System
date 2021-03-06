package at.shehata.ex4.nmea.messages

import at.shehata.ex4.nmea.SatType
import at.shehata.ex4.nmea.interfaces.Message
import at.shehata.ex4.nmea.satellites.*
import java.nio.CharBuffer

/**
 * Object that parses and holds the data of a GSV message
 *
 * @param mTotalMsgs Total number of GSV messages (GSV block)
 * @param mMsgNr the current message
 * @param mTotalSats Total number of Satellites in a GSV block
 * @param mSats List of the infos of each Satellite
 */
data class GSV(
	val mTotalMsgs: UInt?,
	val mMsgNr: UInt?,
	val mTotalSats: UInt?,
	val mSats: List<SatelliteInfo>
) : Message {
	companion object {
		/**
		 * Takes a GSV sentence and returns a GSV Object
		 *
		 * @param _buffer the sentence to parse
		 * @param _satType the current GNSS Satellite (ex. GPS or Galileo) for the
		 * Satellite List
		 *
		 * @return The parsed GSV Message
		 */
		fun handle(_buffer: CharBuffer, _satType: SatType): GSV {
			val data = arrayOfNulls<CharBuffer>(20)

			var i = 0
			var delimiterIndex: Int
			while (_buffer
					.indexOf(',')
					.also { delimiterIndex = it } != -1
			) {
				data[i++] = _buffer
					.slice(_buffer.position(), delimiterIndex)
					.ifEmpty { null }
				_buffer.position(_buffer.position() + delimiterIndex + 1)
			}

			delimiterIndex = _buffer.indexOf('*')
			data[i] = _buffer
				.slice(_buffer.position(), delimiterIndex)
				.ifEmpty { null }
			_buffer.position(_buffer.position() + delimiterIndex + 1)

			return GSV(
				data[0]
					.toString()
					.toUInt(),
				data[1]
					.toString()
					.toUInt(),
				data[2]
					.toString()
					.toUInt(),
				data
					.sliceArray(IntRange(3, data.size - 2))
					.toList()
					.chunked(4)
					.mapNotNull { chunk ->
						if (chunk.all { it == null }) null else chunk
					}
					.map { satInfo ->
						val id = satInfo[0]
							?.toString()
							?.toInt() ?: 0
						val elevation = satInfo[1]
							?.toString()
							?.toDouble() ?: 0.0
						val azimuth = satInfo[2]
							?.toString()
							?.toDouble() ?: 0.0
						val snr = satInfo[3]
							?.toString()
							?.toInt() ?: 0

						when (_satType) {
							SatType.BD -> BeidouSat(id, elevation, azimuth, snr)
							SatType.GA -> GalileoSat(id, elevation, azimuth, snr)
							SatType.GP -> GPSSat(id, elevation, azimuth, snr)
							SatType.GL -> GLONASSSat(id, elevation, azimuth, snr)
							SatType.GN -> SatelliteInfo(id, elevation, azimuth, snr)
						}
					}
			)
		}
	}
}
