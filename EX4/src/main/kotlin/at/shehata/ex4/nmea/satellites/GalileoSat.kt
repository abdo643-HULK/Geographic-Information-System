package at.shehata.ex4.nmea.satellites

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

/**
 * Class that represents a Galileo Satellite
 * and changes the draw function and fillColor
 */
data class GalileoSat(
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
	override val fillColor: Color = Color.RED

	override fun draw(_ctx: GraphicsContext) {
		_ctx.fill = fillColor
		_ctx.fillRoundRect(mPosX - CENTER, mPosY - CENTER, DRAW_SIZE, DRAW_SIZE, 5.0, 5.0)
		_ctx.fill = Color.BLACK
		_ctx.fillText("$mId", mPosX, mPosY)
	}
}