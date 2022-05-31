package at.shehata.ex4.nmea.satellites

data class GPSSat(
	override val mId: Int,
	override val mElevation: Int,
	override val mAzimuth: Int,
	override val mSNR: Int
) : SatelliteInfo(
	mId,
	mElevation,
	mAzimuth,
	mSNR
) {
}