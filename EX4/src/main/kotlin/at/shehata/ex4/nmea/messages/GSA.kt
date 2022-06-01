package at.shehata.ex4.nmea.messages

import at.shehata.ex4.nmea.SatType
import java.nio.CharBuffer

data class GSA(
    val mSelectionMode: Char,
    val mMode: UInt,
    val mIds: List<UInt?>,
    val mPDOP: Float?,
    val mHDOP: Float?,
    val mVDOP: Float?,
) {
    companion object {
        fun handle(_buffer: CharBuffer, _satType: SatType): GSA {
            val data = arrayOfNulls<CharBuffer>(18)

            var i = 0
            var delimiter: Int
            while (_buffer.indexOf(',')
                    .also { delimiter = it } != -1
            ) {
                data[i++] = _buffer.slice(_buffer.position(), delimiter)
                    .ifEmpty { null }
                _buffer.position(_buffer.position() + delimiter + 1)
            }

            delimiter = _buffer.indexOf('*')
            data[i] = _buffer.slice(_buffer.position(), delimiter)
                .ifEmpty { null }
            _buffer.position(_buffer.position() + delimiter + 1)

            val ids = data.drop(2)
                .dropLast(4)
                .map {
                    it?.toString()
                        ?.toUInt()
                }

            return GSA(
                mSelectionMode = data[0]!![0],
                mMode = data[1]!![0].toString()
                    .toUInt(),
                mIds = ids,
                mPDOP = data[14]?.toString()
                    ?.toFloat(),
                mHDOP = data[15]?.toString()
                    ?.toFloat(),
                mVDOP = data[16]?.toString()
                    ?.toFloat()
            )
        }
    }
}