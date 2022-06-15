package at.shehata.ex4.nmea.satellites

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

/**
 * The Information received from GSV Data for each Satellite
 *
 * @param mId the id of the Satellite
 * @param mElevation angular distance relative to the horizontal plane
 * @param mAzimuth angular distance relative to north
 * @param mSNR SNR in dB
 */
open class SatelliteInfo(
	open val mId: Int,
	open val mElevation: Double,
	open val mAzimuth: Double,
	open val mSNR: Int
) {
	companion object {
		/**
		 * The size of the drawn shape of SatelliteInfo
		 */
		const val DRAW_SIZE = 24.0

		/**
		 * The center of the SatelliteInfo Shape to draw
		 */
		const val CENTER = DRAW_SIZE / 2
	}

	/**
	 * The color of the form to draw
	 */
	protected open val fillColor: Color = Color.AZURE

	/**
	 * The current X position on the grid
	 */
	var mPosX: Double = 0.0

	/**
	 * The current Y position on the grid
	 */
	var mPosY: Double = 0.0

	/**
	 * The function draws a shape and a text with the satellites ID
	 *
	 * @param _ctx The graphics context to draw on
	 */
	open fun draw(_ctx: GraphicsContext) {
		_ctx.fill = fillColor
		_ctx.fillRoundRect(mPosX - CENTER, mPosY - CENTER, DRAW_SIZE, DRAW_SIZE, 5.0, 5.0)
		_ctx.fill = Color.BLACK
		_ctx.fillText("$mId", mPosX, mPosY)
	}

	override fun toString() = "SatelliteInfo(mId=$mId, mElevation=$mElevation, mAzimuth=$mAzimuth, mSNR=$mSNR)"
}