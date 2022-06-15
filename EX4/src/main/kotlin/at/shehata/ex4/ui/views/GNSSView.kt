package at.shehata.ex4.ui.views

import at.shehata.ex4.nmea.NMEAParser
import at.shehata.ex4.ui.components.DataView
import at.shehata.ex4.ui.components.DeviationView
import at.shehata.ex4.ui.components.SatView
import at.shehata.ex4.ui.components.TimeView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority

/**
 * The main view for the App
 *
 * @param mParser The parser for the NMEA messages
 */
class GNSSView(private val mParser: NMEAParser) : BorderPane() {
	init {
		start(mParser)
	}

	/**
	 * initializes all the UI elements and attaches them
	 * to the View.
	 *
	 * @param _parser The NMEA parser to push the views to
	 */
	private fun start(_parser: NMEAParser) {
		top = TimeView().apply {
			_parser.addObserver(this)
		}

		center = HBox().apply {
			val mSatView = SatView().apply {
				HBox.setHgrow(this, Priority.ALWAYS)
				_parser.addObserver(this)
			}
			val mDeviationView = DeviationView().apply {
				HBox.setHgrow(this, Priority.ALWAYS)
				_parser.addObserver(this)
			}

			children += arrayOf(mSatView, mDeviationView)
		}

		bottom = DataView().apply {
			_parser.addObserver(this)
		}
	}

	/**
	 * cleans the resources
	 */
	fun close() {
		mParser.close()
	}
}
