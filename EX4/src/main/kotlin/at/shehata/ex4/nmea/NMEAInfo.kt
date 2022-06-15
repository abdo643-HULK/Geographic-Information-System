package at.shehata.ex4.nmea

import at.shehata.ex4.nmea.messages.GGA
import at.shehata.ex4.nmea.satellites.SatelliteInfo
import java.nio.CharBuffer

/**
 * Object that holds all the information necessary for the UpdateListeners
 * to display
 *
 * @param mTime UTC-Time
 * @param mLatitude The Latitude of the position
 * @param mLongitude The Longitude of the position
 * @param mAltitude The Altitude of the position
 * @param mSatelliteCount Number of Satellites used to calculate the position
 * @param mPDOP Position Dilution Of Precision (3D)
 * @param mHDOP Horizontal Dilution Of Precision (2D)
 * @param mVDOP Vertical Dilution Of Precision (1D)
 * @param mQuality Fix-Quality (0 – no fix, 1 – GPS fix, 2 – Differential GPS fix)
 * @param mSatellites List of the infos of each Satellite
 */
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