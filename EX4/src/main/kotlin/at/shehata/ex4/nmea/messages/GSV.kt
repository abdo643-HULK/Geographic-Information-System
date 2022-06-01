package at.shehata.ex4.nmea.messages

import at.shehata.ex4.nmea.SatType
import at.shehata.ex4.nmea.satellites.*
import java.nio.CharBuffer


data class GSV(
    val mTotalMsgs: UInt,
    val mMsgNr: UInt,
    val mTotalSats: UInt,
    val mSats: List<SatelliteInfo>
) {
    companion object {
        fun handle(_buffer: CharBuffer, _satType: SatType): GSV {
            val data = arrayOfNulls<CharBuffer>(19)

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
                    .sliceArray(IntRange(3, data.size - 1))
                    .toList()
                    .chunked(4)
                    .map { ids ->
                        val params = ids.map {
                            it?.toString()
                                ?.toInt() ?: 0
                        }
                        when (_satType) {
                            SatType.BD -> BeidouSat(params[0], params[1], params[2], params[3])
                            SatType.GA -> GalileoSat(params[0], params[1], params[2], params[3])
                            SatType.GP -> GPSSat(params[0], params[1], params[2], params[3])
                            SatType.GL -> GLONASSSat(params[0], params[1], params[2], params[3])
                            else -> SatelliteInfo(params[0], params[1], params[2], params[3])
                        }
                    }
            )
        }
    }
}
