package at.shehata.ex4.nmea

import at.shehata.ex4.nmea.messages.GGA
import at.shehata.ex4.nmea.satellites.SatelliteInfo
import java.nio.CharBuffer

data class NMEAInfo(
	var mTime: CharBuffer? = null,
	var mWidth: Float? = null,
	var mHeight: Float? = null,
	var mSatelliteCount: UInt? = null,
	var mPDOP: Float? = null,
	var mHDOP: Float? = null,
	var mVDOP: Float? = null,
	var mQuality: GGA.FixQuality? = null,
	val mList: MutableList<SatelliteInfo> = mutableListOf()
)