package at.shehata.ex4.nmea.satellites

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

open class SatelliteInfo(
	open val mId: Int,
	open val mElevation: Double,
	open val mAzimuth: Double,
	open val mSNR: Int
) {
	companion object {
		const val DRAW_SIZE = 24.0
		const val CENTER = DRAW_SIZE / 2
	}

	protected open val fillColor: Color = Color.AZURE

	var mPosX: Double = 0.0
	var mPosY: Double = 0.0

	open fun draw(_ctx: GraphicsContext) {
		_ctx.fill = fillColor
		_ctx.fillRoundRect(mPosX - CENTER, mPosY - CENTER, DRAW_SIZE, DRAW_SIZE, 5.0, 5.0)
		_ctx.fill = Color.BLACK
		_ctx.fillText("$mId", mPosX, mPosY)
	}

	override fun toString() = "SatelliteInfo(mId=$mId, mElevation=$mElevation, mAzimuth=$mAzimuth, mSNR=$mSNR)"
}