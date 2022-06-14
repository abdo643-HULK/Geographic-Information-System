package at.shehata.ex4.ui.components

import at.shehata.ex4.GISApplication
import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.NMEAInfo
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


class SatView : Pane(), PositionUpdateListener {
	private val mCanvas = Canvas().apply {
		widthProperty().bind(this@SatView.widthProperty())
		heightProperty().bind(this@SatView.heightProperty())

		widthProperty().addListener { _, _, _ -> draw() }
		heightProperty().addListener { _, _, _ -> draw() }
	}


	private val mCtx = mCanvas.graphicsContext2D

	init {
		setMinSize(0.0, 0.0)
		background = Background.fill(Color.rgb(211, 211, 211))
		children += mCanvas
	}

	private fun draw(_info: NMEAInfo? = null) {
		mCtx.apply {
			clearRect(0.0, 0.0, canvas.width, canvas.height)
			drawInnerCircle()
			drawOuterCircle()

			val diagonal = width - 50
			val radius = diagonal / 2
			val centerX = width / 2
			val centerY = height / 2

			val offset = 15

			strokeLine(centerX, centerY - radius - offset, centerX, centerY + radius + offset)
			strokeLine(centerX - radius - offset, centerY, centerX + radius + offset, centerY)

			_info?.apply {
				mSatellites.forEach {
					val distance = cos(toRadians(it.mElevation)) * radius
					it.mPosX = centerX + distance * sin(toRadians(it.mAzimuth))
					it.mPosY = centerY - distance * cos(toRadians(it.mAzimuth))

					mCtx.textAlign = TextAlignment.CENTER
					mCtx.textBaseline = VPos.CENTER
					it.draw(mCtx)
				}
			}

			font = Font.font(GISApplication.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 18.0)
			fillText("N", centerX + 8, centerY - radius - offset)
			fillText("S", centerX + 8, centerY + radius + offset)

			fillText("W", centerX - radius - 12, centerY - 10)
			fillText("E", centerX + radius + 12, centerY - 10)
		}
	}

	private fun drawOuterCircle() {
		mCtx.apply {
			stroke = Color.BLACK
			drawCircle(this, width - 50)
		}
	}

	private fun drawInnerCircle() {
		mCtx.apply {
			stroke = Color.BLACK
			drawCircle(this, width - 150)
		}
	}

	override fun update(_info: NMEAInfo) {
		draw(_info)
	}
}