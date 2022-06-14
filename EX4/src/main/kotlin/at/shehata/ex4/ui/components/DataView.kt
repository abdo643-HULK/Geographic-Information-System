package at.shehata.ex4.ui.components

import at.shehata.ex4.GISApplication
import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.NMEAInfo
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text

class DataView : HBox(), PositionUpdateListener {

	private val mTexts by lazy {
		arrayOf(
			"Latitude" to 6,
			"Longitude" to 6,
			"Altitude" to 6,
			"PDOP" to 3,
			"HDOP" to 3,
			"VDOP" to 3,
		).map {
			arrayOf(
				Text().apply {
					fill = Color.WHITE
					font = Font.font(GISApplication.DEFAULT_FONT_FAMILY, FontWeight.BOLD, 16.0)
					text = it.first
				},
				Text().apply {
					fill = Color.WHITE
					font = Font.font(GISApplication.DEFAULT_FONT_FAMILY, 16.0)
					text = "%.${it.second}f".format(0f)
				}
			)
		}
	}

	init {
		background = Background.fill(Color.rgb(31, 178, 170))
		padding = Insets(10.0)
		alignment = Pos.CENTER

		spacing = 30.0
		children += mTexts.map {
			HBox().apply {
				spacing = 10.0
				children += it
			}
		}
	}

	override fun update(_info: NMEAInfo) {
		mTexts[0][1].text = "%.6f".format(_info.mLatitude ?: 0f)
		mTexts[1][1].text = "%.6f".format(_info.mLongitude ?: 0f)
		mTexts[2][1].text = "%.6f".format(_info.mAltitude ?: 0f)
		mTexts[3][1].text = "%.2f".format(_info.mPDOP ?: 0f)
		mTexts[4][1].text = "%.2f".format(_info.mHDOP ?: 0f)
		mTexts[5][1].text = "%.2f".format(_info.mVDOP ?: 0f)
	}
}