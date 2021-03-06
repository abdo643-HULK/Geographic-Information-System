package at.shehata.ex4.ui.components

import at.shehata.ex4.GISApplication
import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.NMEAInfo
import at.shehata.ex4.nmea.satellites.SatelliteInfo
import at.shehata.ex4.utils.drawCircle
import javafx.geometry.VPos
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Background
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin

/**
 * View that shows the satellites
 * positions
 */
class SatView : Pane(), PositionUpdateListener {
	/**
	 * The drawing canvas
	 */
	private val mCanvas = Canvas().apply {
		widthProperty().run {
			bind(this@SatView.widthProperty())
			addListener { _, _, _ -> draw() }
		}
		heightProperty().run {
			bind(this@SatView.heightProperty())
			addListener { _, _, _ -> draw() }
		}
	}

	/**
	 * The graphics context of the canvas
	 */
	private val mCtx = mCanvas.graphicsContext2D

	/**
	 * Diagonal for the Outer Circle
	 */
	private val mDiagonal
		get() = mCanvas.width - 50

	/**
	 * List of satellites that are used
	 */
	private var mSatellites = emptyList<SatelliteInfo>()

	init {
		setMinSize(0.0, 0.0)
		background = Background.fill(Color.rgb(211, 211, 211))
		children += mCanvas
	}

	/**
	 * Draws the Coordinate System and the Satellites positions
	 */
	private fun draw() {
		mCtx.apply {
			clearRect(0.0, 0.0, canvas.width, canvas.height)
			drawInnerCircle()
			drawOuterCircle()

			val radius = mDiagonal / 2
			val centerX = width / 2
			val centerY = height / 2

			val offset = 15

			strokeLine(centerX, centerY - radius - offset, centerX, centerY + radius + offset)
			strokeLine(centerX - radius - offset, centerY, centerX + radius + offset, centerY)

			mSatellites.forEach {
				val distance = cos(toRadians(it.mElevation)) * radius
				it.mPosX = centerX + distance * sin(toRadians(it.mAzimuth))
				it.mPosY = centerY - distance * cos(toRadians(it.mAzimuth))

				mCtx.textAlign = TextAlignment.CENTER
				mCtx.textBaseline = VPos.CENTER
				it.draw(mCtx)
			}

			font = Font.font(GISApplication.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 18.0)
			fillText("N", centerX + 8, centerY - radius - offset)
			fillText("S", centerX + 8, centerY + radius + offset)

			fillText("W", centerX - radius - 12, centerY - 10)
			fillText("E", centerX + radius + 12, centerY - 10)
		}
	}

	/**
	 * Draws the outer circle on the canvas
	 */
	private fun drawOuterCircle() {
		mCtx.apply {
			stroke = Color.BLACK
			drawCircle(this, mDiagonal)
		}
	}


	/**
	 * Draws the inner circle on the canvas
	 * with 45 degree "distance"
	 */
	private fun drawInnerCircle() {
		mCtx.apply {
			stroke = Color.BLACK
			drawCircle(this, cos(toRadians(45.0)) * mDiagonal)
		}
	}

	/**
	 * Reads the info about the used satellites
	 * and updates the view.
	 *
	 * @param _info the object holding the satellites
	 */
	override fun update(_info: NMEAInfo) {
		mSatellites = _info.mSatellites
		draw()
	}
}