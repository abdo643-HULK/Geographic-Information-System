package at.shehata.ex4.nmea.messages

import at.shehata.ex4.nmea.SatType
import java.nio.CharBuffer


data class GGA(
    val mTime: CharBuffer?,
    val mLatitude: CharBuffer?,
    val mLatDir: LatDirection?,
    val mLongitude: CharBuffer?,
    val mLongDir: LongDirection?,
    val mQuality: FixQuality?,
    val mSatelliteCount: UInt?,
    val mHDOP: Float?,
    val mGeoidHeight: Float?,
    val mHeightUnit: CharBuffer?,
    val mDifference: Float?,
    val mDifferenceUnit: CharBuffer?,
    val mAge: Float?,
    val mId: Int?
) {

    enum class LatDirection {
        N, S
    }

    enum class LongDirection {
        E, W
    }

    enum class FixQuality(val mVal: UInt) {
        NO_FIX(0u),
        GPS_FIX(1u),
        DIFFERENTIAL_GPS_FIX(2u);

        companion object {
            fun from(_s: UInt): FixQuality? = values().find { it.mVal == _s }
        }
    }

    companion object {
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
                mGeoidHeight = data[8]
                    ?.toString()
                    ?.toFloat(),
                mHeightUnit = data[9],
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