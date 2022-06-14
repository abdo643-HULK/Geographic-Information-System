package at.shehata.ex4.nmea

import at.shehata.ex4.nmea.messages.GGA
import at.shehata.ex4.nmea.satellites.SatelliteInfo
import java.nio.CharBuffer

data class NMEAInfo(
	var mTime: CharBuffer? = null,
	var mLatitude: Float? = null,
	var mLongitude: Float? = null,
	var mAltitude: Float? = null,
	var mSatelliteCount: UInt? = null,
	var mPDOP: Float? = null,
	var mHDOP: Float? = null,
	var mVDOP: Float? = null,
	var mQuality: GGA.FixQuality? = null,
	val mSatellites: MutableList<SatelliteInfo> = mutableListOf()
)