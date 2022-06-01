package at.shehata.ex4.ui.components

import at.shehata.ex4.GISApplication
import at.shehata.ex4.interfaces.PositionUpdateListener
import javafx.scene.canvas.Canvas
import javafx.scene.layout.HBox
import java.awt.Image

class SatView : HBox(), PositionUpdateListener {

    fun draw() {
        setMinSize(0.0, 0.0)
        val width = widthProperty()
        val height = heightProperty()
        val canvas = Canvas().apply {
            id = GISApplication.CANVAS_ID
            widthProperty().bind(width)
            heightProperty().bind(height)
        }
        val overlay = Canvas().apply {
            id = GISApplication.OVERLAY_ID
            widthProperty().bind(width)
            heightProperty().bind(height)
        }
        children += arrayOf(canvas, overlay)
    }

    override fun update(_img: Image, _scale: Int) {
        TODO("Not yet implemented")
    }
}