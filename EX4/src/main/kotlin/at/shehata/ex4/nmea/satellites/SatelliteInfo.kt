package at.shehata.ex4.nmea.satellites

open class SatelliteInfo(
	open val mId: Int,
	open val mElevation: Int,
	open val mAzimuth: Int,
	open val mSNR: Int
) {
	override fun toString() = "SatelliteInfo(mId=$mId, mElevation=$mElevation, mAzimuth=$mAzimuth, mSNR=$mSNR)"
}