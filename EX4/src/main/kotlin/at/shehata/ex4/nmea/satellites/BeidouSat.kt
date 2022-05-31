package at.shehata.ex4.nmea.satellites

class BeidouSat(
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