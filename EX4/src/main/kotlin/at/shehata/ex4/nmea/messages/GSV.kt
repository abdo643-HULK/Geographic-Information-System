package at.shehata.ex4.nmea.messages

import at.shehata.ex4.nmea.satellites.SatelliteInfo
import java.nio.CharBuffer

data class GSV(
	val mTotalMsgs: UInt,
	val mMsgNr: UInt,
	val mTotalSats: UInt,
	val mSats: List<SatelliteInfo>
) {
	companion object {
		fun handle(_buffer: CharBuffer): GSV {
			var delimiterIndex: Int
			val data = arrayOfNulls<CharBuffer>(19)
			var i = 0
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
//				satellites
				data
					.sliceArray(IntRange(3, data.size - 1))
					.toList()
					.chunked(4)
					.map {
						SatelliteInfo(
							it[0]
								?.toString()
								?.toInt() ?: 0,
							it[1]
								?.toString()
								?.toInt() ?: 0,
							it[2]
								?.toString()
								?.toInt() ?: 0,
							it[3]
								?.toString()
								?.toInt() ?: 0
						)
					}
			)
		}
	}
}
