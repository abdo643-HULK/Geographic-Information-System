package at.shehata.ex4.ui.components

import at.shehata.ex4.GISApplication
import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.NMEAInfo
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.CacheHint
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeType
import javafx.scene.text.Font
import javafx.scene.text.Text
import java.text.SimpleDateFormat
import java.util.*


/**
 * A View to show the time from NMEAInfo
 */
class TimeView : HBox(), PositionUpdateListener {

	/**
	 * Creates a Text Object and initializes it
	 */
	private val mTime by lazy {
		Text().apply {
			fill = Color.WHITE
			font = Font(GISApplication.DEFAULT_FONT_FAMILY, 32.0)
			stroke = Color.BLACK
			strokeWidth = 2.0
			strokeType = StrokeType.OUTSIDE
			cacheHint = CacheHint.QUALITY
			text = "00:00:00"
		}
	}

	init {
		background = Background.fill(Color.web("#BDB76B"))
		padding = Insets(5.0)
		alignment = Pos.CENTER

		children += mTime
	}

	/**
	 * Convert UTC time to local time
	 *
	 * @param _time the time to convert
	 * @return the converted time
	 */
	private fun convertUtcToLocale(_time: String) = SimpleDateFormat("HH:mm:ss").run {
		timeZone = TimeZone.getTimeZone("UTC")
		val date = parse(_time)
		timeZone = TimeZone.getDefault()
		format(date)
	}

	/**
	 * Reads the time and converts it to the local time
	 * and updates the view
	 *
	 * @param _info the object holding the time
	 */
	override fun update(_info: NMEAInfo) {
		val hour = _info.mTime?.slice(0, 2) ?: return
		val minute = _info.mTime?.slice(2, 2) ?: return
		val seconds = _info.mTime?.slice(4, 2) ?: return
		val utcTime = "$hour:$minute:$seconds"

		mTime.text = convertUtcToLocale(utcTime)
	}
}