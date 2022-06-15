package at.shehata.ex4.nmea

import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.interfaces.Message
import at.shehata.ex4.nmea.messages.GGA
import at.shehata.ex4.nmea.messages.GSA
import at.shehata.ex4.nmea.messages.GSV
import at.shehata.ex4.utils.GNSSSimulator
import at.shehata.ex4.utils.safeValueOf
import kotlinx.coroutines.*
import java.nio.CharBuffer

/**
 * The supported Satellites
 */
enum class SatType {
	GP,
	GA,
	GL,
	GN,
	BD;

	companion object {
		/**
		 * Parses a value without throwing
		 */
		fun safeValueOf(_value: String) = safeValueOf<SatType>(_value)
	}
}

/**
 * The supported Message-types
 */
enum class MsgType {
	GGA,
	GSA,
	GSV;

	companion object {
		/**
		 * The length of a message type
		 */
		const val LENGTH = 3

		/**
		 * Parses a value without throwing
		 */
		fun safeValueOf(_value: String) = safeValueOf<MsgType>(_value)
	}
}

/**
 * Start Character of a message
 */
const val START_DELIMITER = '$'

/**
 * Character before the checksum
 */
const val CHECKSUM_DELIMITER = '*'

private class InvalidMessage(_msg: String = "") : Error("Invalid Sentence received. $_msg")

/**
 * A class that parses NMEA messages
 */
class NMEAParser {
	/**
	 * A coroutine to handle parsing on another thread.
	 */
	private val mScope = CoroutineScope(Dispatchers.Default)

	/**
	 * The Listeners to notify when a block has been parsed
	 */
	private val mListeners = mutableListOf<PositionUpdateListener>()

	/**
	 * The source for the parser to parse
	 */
	private val mGNSS by lazy {
		GNSSSimulator(
			javaClass.getResource("/nmea/data-2.nmea")!!.file,
			1000,
			"GGA"
		)
	}

	/**
	 * The object to send to the observers
	 */
	private var mInfo = NMEAInfo()

	/**
	 * A copy of the mInfo
	 */
	private var mOldInfo = NMEAInfo()

	/**
	 * Checks the sum and if it's invalid a failed Result is returned else
	 * a success is returned
	 *
	 * @param _sentence The sentence to validate
	 * @return successful result if the sum is valid else a failure
	 */
	private fun validateSum(_sentence: String): Result<Unit> {
		var pos = 0
		if (_sentence[pos++] != START_DELIMITER) return Result.failure(InvalidMessage("No '$START_DELIMITER'"))

		var checksum = 0x00
		while (_sentence[pos] != CHECKSUM_DELIMITER) {
			checksum = checksum xor _sentence[pos++].code
			if (pos >= _sentence.length - 1) return Result.failure(InvalidMessage("No '$CHECKSUM_DELIMITER' found"))
		}

		if (_sentence[pos++] != CHECKSUM_DELIMITER) return Result.failure(InvalidMessage())

		val expected = Integer.valueOf(_sentence[pos++] + _sentence[pos].toString(), 16)

		if (expected != checksum) return Result.failure(
			InvalidMessage(
				"Checksum calculated: ${checksum.toString(16)} but expected ${
					expected.toString(
						16
					)
				}"
			)
		)

		return Result.success(Unit)
	}


	/**
	 * Parses a sentence. If invalid an InvalidMessage is thrown else a Message object is returned
	 *
	 * @param _sentence The sentence to parse
	 * @return Object that represents a Message
	 */
	@Throws(InvalidMessage::class)
	private fun parseConventional(_sentence: String): Message? {
		validateSum(_sentence).getOrThrow()

		val buffer = CharBuffer
			.wrap(_sentence)
			.position(1)

		val satType = SatType.safeValueOf(
			buffer
				.slice(buffer.position(), 2)
				.toString()
		) ?: return null

		val handler = when (
			MsgType.safeValueOf(
				buffer
					.slice(buffer.position() + 2, MsgType.LENGTH)
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

	/**
	 * Sets the data of the GGA object on the NMEAInfo object
	 *
	 * @param _data the GGA object that holds the data
	 */
	private fun extractGGA(_data: GGA) {
		update()
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

	/**
	 * Sets the data of the GSA object on the NMEAInfo object
	 *
	 * @param _data the GSA object that holds the data
	 */
	private fun extractGSA(_data: GSA) {
		_data.apply {
			mInfo.mHDOP = mHDOP ?: mInfo.mHDOP
			mInfo.mPDOP = mPDOP ?: mInfo.mPDOP
			mInfo.mVDOP = mVDOP ?: mInfo.mVDOP
		}
	}

	/**
	 * Sets the data of the GSV object on the NMEAInfo object
	 *
	 * @param _data the GSV object that holds the data
	 */
	private fun extractGSV(_data: GSV) {
		_data.apply {
			mInfo.mSatelliteCount = mTotalSats ?: mInfo.mSatelliteCount
			mInfo.mSatellites += mSats
		}
	}

	/**
	 * Sets properties of the NMEAInfo object
	 * depending on the message type
	 */
	private fun setNmeaInfo(_data: Message) {
		when (_data) {
			is GGA -> extractGGA(_data)
			is GSV -> extractGSV(_data)
			is GSA -> extractGSA(_data)
		}
	}

	fun parse() = mScope.launch { parseNonBlocking() }

	/**
	 * Pushes an observer to the list of listeners
	 *
	 * @param _observer The observer to add
	 */
	fun addObserver(_observer: PositionUpdateListener) = mListeners.add(_observer)

	/**
	 * Informs the Listeners about the parsed block
	 */
	private fun update() = mListeners.forEach { it.update(mInfo) }

	/**
	 * Parses a Reader for NMEA Data line by line on another thread
	 */
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

	/**
	 * Cleans up resources
	 */
	fun close() {
		mScope.cancel("Parser closed")
	}
}
