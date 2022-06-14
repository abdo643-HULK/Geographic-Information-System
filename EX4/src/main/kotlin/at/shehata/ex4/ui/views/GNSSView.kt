package at.shehata.ex4.ui.views

import at.shehata.ex4.nmea.NMEAParser
import at.shehata.ex4.ui.components.DataView
import at.shehata.ex4.ui.components.DeviationView
import at.shehata.ex4.ui.components.SatView
import at.shehata.ex4.ui.components.TimeView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority


class GNSSView(_parser: NMEAParser) : BorderPane() {
    init {
        start(_parser)
    }

    /**
     * initializes all the UI elements and attaches them
     * to the View.
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
}
