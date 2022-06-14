package at.shehata.ex4.nmea

import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.interfaces.Message
import at.shehata.ex4.nmea.messages.GGA
import at.shehata.ex4.nmea.messages.GSA
import at.shehata.ex4.nmea.messages.GSV
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

private class InvalidMessage(_msg: String) : Error(_msg)

class NMEAParser {
	private val mScope = CoroutineScope(Dispatchers.Main)

	private val mListeners = mutableListOf<PositionUpdateListener>()

	private val mGNSS by lazy {
		GNSSSimulator(
			javaClass.getResource("/nmea/data-2.nmea")!!.file,
			1000,
			"GGA"
		)
	}

	private var mInfo = NMEAInfo()
	private var mOldInfo = NMEAInfo()


	fun parse() {
		mScope.launch { parseNonBlocking() }
	}


	private fun validateSum(_sentence: String): Pair<Boolean, String> {
		var pos = 0
		if (_sentence[pos++] != START_DELIMITER) return Pair(false, "No '$START_DELIMITER'")

		var checksum = 0x00
		while (_sentence[pos] != CHECKSUM_DELIMITER) {
			checksum = checksum xor _sentence[pos++].code
			if (pos >= _sentence.length - 1) return Pair(false, "No '$CHECKSUM_DELIMITER' found")
		}

		if (_sentence[pos++] != CHECKSUM_DELIMITER) return Pair(false, "")

		val expected = Integer.valueOf(_sentence[pos++] + _sentence[pos].toString(), 16)

		return Pair(
			expected == checksum,
			"Checksum calculated: ${checksum.toString(16)} but expected ${expected.toString(16)}"
		)
	}


	@Throws(InvalidMessage::class)
	private fun parseConventional(_sentence: String): Message? {
		val (isValid, errorMsg) = validateSum(_sentence)
		if (!isValid) throw InvalidMessage("Invalid Sentence received. $errorMsg")

		val buffer = CharBuffer
			.wrap(_sentence)
			.position(1)

		val satType = safeValueOf<SatType>(
			buffer
				.slice(buffer.position(), 2)
				.toString()
		) ?: return null

		val handler = when (
			safeValueOf<MsgType>(
				buffer
					.slice(buffer.position() + 2, MSG_TYPE_LENGTH)
					.toString()
			)
		) {
			MsgType.GGA -> GGA::handle
			MsgType.GSV -> GSV::handle
			MsgType.GSA -> GSA::handle
			else -> return null
		}

		return handler(buffer.position(buffer.position() + 6), satType)
	}

	private fun extractGGA(_data: GGA) {
		mListeners.forEach { it.update(mInfo) }
		mOldInfo = mInfo

		mInfo = NMEAInfo()
		_data.apply {
			mInfo.mTime = mTime ?: mInfo.mTime
			mInfo.mQuality = mQuality ?: mInfo.mQuality
		}

		mInfo.mLatitude = _data.mLatitude?.let {
			val (grad, minutes) = it
				.slice(0, 2)
				.toString()
				.toInt() to it
				.slice(2, it.length - 2)
				.toString()
				.toFloat()
			grad + minutes / 60
		} ?: mInfo.mLatitude

		mInfo.mLongitude = _data.mLongitude?.let {
			val (grad, minutes) = it
				.slice(0, 3)
				.toString()
				.toInt() to it
				.slice(3, it.length - 3)
				.toString()
				.toFloat()
			grad + minutes / 60
		} ?: mInfo.mLongitude

		mInfo.mAltitude = _data.mAltitude ?: mInfo.mAltitude
	}

	private fun extractGSA(_data: GSA) {
		_data.apply {
			mInfo.mHDOP = mHDOP ?: mInfo.mHDOP
			mInfo.mPDOP = mPDOP ?: mInfo.mPDOP
			mInfo.mVDOP = mVDOP ?: mInfo.mVDOP
		}
	}

	private fun extractGSV(_data: GSV) {
		_data.apply {
			mInfo.mSatelliteCount = mTotalSats ?: mInfo.mSatelliteCount
			mInfo.mSatellites += mSats
		}
	}

	private fun setNmeaInfo(_data: Message) {
		when (_data) {
			is GGA -> extractGGA(_data)
			is GSV -> extractGSV(_data)
			is GSA -> extractGSA(_data)
		}
	}

	private suspend fun parseNonBlocking() = withContext(Dispatchers.Default) {
		mGNSS.useLines { lines ->
			lines.forEach { line ->
				try {
					val data = when (line[0]) {
						'$' -> parseConventional(line) ?: return@forEach
						else -> return@forEach
					}
					setNmeaInfo(data)
				} catch (_e: Exception) {
					System.err.println(_e.message)
				} catch (_e: Error) {
					System.err.println(_e.message)
				}
			}
		}
		println("finished parsing")
	}

	fun addObserver(_observer: PositionUpdateListener) = mListeners.add(_observer)
}
