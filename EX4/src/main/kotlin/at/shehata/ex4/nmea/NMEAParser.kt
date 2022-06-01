package at.shehata.ex4.nmea

import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.messages.GGA
import at.shehata.ex4.nmea.messages.GSA
import at.shehata.ex4.nmea.messages.GSV
import at.shehata.ex4.nmea.satellites.*
import at.shehata.ex4.utils.GNSSSimulator
import at.shehata.ex4.utils.safeValueOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.CharBuffer

enum class SatType {
    GP,
    GA,
    GL,
    GN,
    BD
}

enum class MsgType {
    GGA,
    GSA,
    GSV
}

const val MSG_TYPE_LENGTH = 3

const val START_DELIMITER = '$'
const val CHECKSUM_DELIMITER = '*'


class NMEAParser {
    private val mScope = CoroutineScope(Dispatchers.Main)

    private val mListeners = mutableListOf<PositionUpdateListener>()

    private val mGNSS by lazy {
        GNSSSimulator(
            javaClass.getResource("/nmea/data-2.nmea")!!.file,
            100,
            "GGA"
        )
    }

    private var mInfo = NMEAInfo()


    fun parse() {
        mScope.launch { parseNonBlocking() }
    }


    private fun validateSum(_sentence: String): Boolean {
        var pos = 0
        if (_sentence[pos++] != START_DELIMITER) return false

        var checksum = 0x00
        while (_sentence[pos] != CHECKSUM_DELIMITER) {
            checksum = checksum xor _sentence[pos++].code
        }

        if (_sentence[pos++] != CHECKSUM_DELIMITER) return false

        val expected = Integer.valueOf(_sentence[pos++] + _sentence[pos].toString(), 16)

        return expected == checksum
    }


    private fun parseConventional(_sentence: String): Any? {
        if (!validateSum(_sentence)) return null //throw Exception("Invalid Sentence received")

        val buffer = CharBuffer
            .wrap(_sentence)
            .position(1)

        val satType = safeValueOf<SatType>(
            buffer.slice(buffer.position(), 2)
                .toString()
        ) ?: return null

        val handler = when (safeValueOf<MsgType>(
            buffer.slice(buffer.position() + 2, 3)
                .toString()
        )) {
            MsgType.GGA -> GGA::handle
            MsgType.GSV -> GSV::handle
            MsgType.GSA -> GSA::handle
            else -> return null
        }

        return handler(buffer.position(buffer.position() + 6), satType)
    }

    private fun extractGGA(_data: GGA) {
        println(mInfo)
        mListeners.forEach { it.update(mInfo) }
        mInfo = mInfo.copy()
        _data.apply {
            mInfo.mTime = mTime
            mInfo.mQuality = mQuality
        }
        _data.mLatitude?.apply {
            val (grad, minutes) = slice(0, 2)
                .toString()
                .toInt() to slice(2, length - 2)
                .toString()
                .toFloat()
            mInfo.mWidth = grad + minutes / 60
        }
        _data.mLongitude?.apply {
            val (grad, minutes) = slice(0, 3)
                .toString()
                .toInt() to slice(3, length - 3)
                .toString()
                .toFloat()
            mInfo.mHeight = grad + minutes / 60
        }
    }

    private fun extractGSA(_data: GSA) {
        _data.apply {
            mInfo.mHDOP = mHDOP
            mInfo.mPDOP = mPDOP
            mInfo.mVDOP = mVDOP
        }
    }

    private fun extractGSV(_data: GSV) {
        _data.apply {
            val size = mInfo.mList.size
            mInfo.mSatelliteCount = mTotalSats
            mInfo.mList += mSats.slice(IntRange(0, (mTotalSats.toInt() - size - 1) % 4))
        }
    }

    private fun setNmeaInfo(_data: Any) {
        when (_data) {
            is GGA -> extractGGA(_data)
            is GSV -> extractGSV(_data)
            is GSA -> extractGSA(_data)
        }
    }

    private suspend fun parseNonBlocking() = withContext(Dispatchers.Default) {
        mGNSS.useLines { lines ->
            lines.forEach { line ->
                val data = when (line[0]) {
                    '$' -> parseConventional(line) ?: return@forEach
                    else -> return@forEach
                }

                setNmeaInfo(data)
            }
        }
    }

    fun addObserver(_observer: PositionUpdateListener) = mListeners.add(_observer)
}