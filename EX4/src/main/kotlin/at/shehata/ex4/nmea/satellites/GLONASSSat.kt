package at.shehata.ex4.nmea.satellites

class GLONASSSat(
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
	companion object {
		const val ID = "GL"
	}
}