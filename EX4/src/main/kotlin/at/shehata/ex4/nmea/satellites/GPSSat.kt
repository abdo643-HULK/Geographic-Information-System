package at.shehata.ex4.nmea.satellites

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

/**
 * Class that represents a GPS Satellite
 * and changes the draw function and fillColor
 */
data class GPSSat(
	override val mId: Int,
	override val mElevation: Double,
	override val mAzimuth: Double,
	override val mSNR: Int
) : SatelliteInfo(
	mId,
	mElevation,
	mAzimuth,
	mSNR
) {
	override val fillColor: Color = Color.BROWN

	override fun draw(_ctx: GraphicsContext) {
		_ctx.fill = fillColor
		_ctx.fillRoundRect(mPosX - CENTER, mPosY - CENTER, DRAW_SIZE, DRAW_SIZE, CENTER, CENTER)
		_ctx.fill = Color.BLACK
		_ctx.fillText("$mId", mPosX, mPosY)
	}
}