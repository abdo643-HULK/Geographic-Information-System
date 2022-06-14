package at.shehata.ex4.ui.components

import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.NMEAInfo
import at.shehata.ex4.utils.drawCircle
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import java.awt.Image

class DeviationView : Pane(), PositionUpdateListener {

	private val mCanvas = Canvas().apply {
		background = Background.fill(Color.BLUE)
		widthProperty().bind(this@DeviationView.widthProperty())
		heightProperty().bind(this@DeviationView.heightProperty())

		widthProperty().addListener { _, _, _ -> draw() }
		heightProperty().addListener { _, _, _ -> draw() }
	}

	init {
		setMinSize(0.0, 0.0)
		background = Background.fill(Color.rgb(211,211,211))
		children += mCanvas
	}

	private fun draw() {
		mCanvas.graphicsContext2D.apply {
			clearRect(0.0, 0.0, canvas.width, canvas.height)
			stroke = Color.BLACK
			drawCircle(this, width - 50)
		}
	}


	override fun update(_info: NMEAInfo) {
	}
}