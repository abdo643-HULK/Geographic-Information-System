package at.shehata.ex4.nmea

import at.shehata.ex4.nmea.messages.GSA
import at.shehata.ex4.nmea.messages.GSV
import at.shehata.ex4.utils.GNSSSimulator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.CharBuffer

enum class Talker {
	GPGGA,
	GPGSA,
	GPGSV;

	companion object {
		const val LENGTH = 5
	}
}

const val START_DELIMITER = '$'
const val CHECKSUM_DELIMITER = '*'


class NMEAParser {
	private val mScope = CoroutineScope(Dispatchers.Main)

	private val mGNSS by lazy {
		GNSSSimulator(
			javaClass.getResource("/nmea/data-1.nmea")!!.file,
			100,
			"GPGGA"
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
		if (!validateSum(_sentence)) return null//throw Exception("Invalid Sentence received")
		val buffer = CharBuffer
			.wrap(_sentence)
			.position(1)

		val handler: (CharBuffer) -> Any = when (buffer.slice(buffer.position(), Talker.LENGTH)) {
			CharBuffer.wrap(Talker.GPGGA.name) -> GGA::handle
			CharBuffer.wrap(Talker.GPGSV.name) -> GSV::handle
			CharBuffer.wrap(Talker.GPGSA.name) -> GSA::handle
			else -> return null
		}

		return handler(buffer.position(buffer.position() + Talker.LENGTH + 1))
	}

	private suspend fun parseNonBlocking() = withContext(Dispatchers.Default) {
		mGNSS.useLines { lines ->
			lines.forEach { line ->
				val data = when (line[0]) {
					'$' -> parseConventional(line) ?: return@forEach
					else -> return@forEach
				}
				when (data) {
					is GGA -> {
						println(mInfo)
						mInfo = mInfo.copy()
						data.apply {
							mInfo.mTime = mTime
							mInfo.mQuality = mQuality
						}
						data.mLatitude?.apply {
							val (grad, minutes) = slice(0, 2)
								.toString()
								.toInt() to slice(2, length - 2)
								.toString()
								.toFloat()
							mInfo.mWidth = grad + minutes / 60
						}
						data.mLongitude?.apply {
							val (grad, minutes) = slice(0, 3)
								.toString()
								.toInt() to slice(3, length - 3)
								.toString()
								.toFloat()
							mInfo.mHeight = grad + minutes / 60
						}
					}
					is GSV -> {
						data.apply {
							val size = mInfo.mList.size
							mInfo.mSatelliteCount = mTotalSats
							mInfo.mList += mSats.slice(IntRange(0, (mTotalSats.toInt() - size - 1) % 4))
						}
					}
					is GSA -> {
						data.apply {
							mInfo.mHDOP = mHDOP
							mInfo.mPDOP = mPDOP
							mInfo.mVDOP = mVDOP
						}
					}
				}
			}
		}
	}
}