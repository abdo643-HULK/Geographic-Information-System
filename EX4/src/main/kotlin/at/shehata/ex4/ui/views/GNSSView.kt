package at.shehata.ex4.ui.views

import at.shehata.ex4.GISApplication
import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.NMEAInfo
import at.shehata.ex4.nmea.NMEAParser
import at.shehata.ex4.ui.components.CanvasPane
import at.shehata.ex4.ui.components.DataView
import at.shehata.ex4.ui.components.TimeView
import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.Canvas
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import java.awt.Image
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage


class GNSSView() : BorderPane(), PositionUpdateListener {
    companion object {
        const val SCALE_FIELD_ID = "scale-field"
    }

    /**
     * The image to render on the canvas
     */
    private lateinit var mImage: BufferedImage

    init {
        start()
    }

    /**
     * initializes all the UI elements and attaches them
     * to the View.
     */
    private fun start() {
//		top = MenuBar(mController.getActionHandler())
//		center = CanvasPae(
//			mController.getMouseHandler(),
//			mController.getChangeHandler(),
//		)
//		bottom = BottomBar(mController.getActionHandler(), mController.getKeyHandler())
//		onKeyPressed = mController.getKeyHandler()
//		onKeyReleased = mController.getKeyHandler()
//		onScroll = mController.getScrollHandler()

        top = TimeView()
        center = CanvasPane()
        bottom = DataView()


        NMEAParser().parse()
    }

    /**
     * renders the image on the canvas
     */
    fun repaint() {
        val writable = SwingFXUtils.toFXImage(mImage, null)
        val canvas = scene.lookup("#${GISApplication.CANVAS_ID}") as Canvas

        canvas.graphicsContext2D.apply {
            clearRect(0.0, 0.0, canvas.width, canvas.height)
            drawImage(writable, 0.0, 0.0)
        }
    }

    fun translate(_dX: Double, _dY: Double) {
        val canvas = scene.lookup("#${GISApplication.CANVAS_ID}") as Canvas
        val width = canvas.width
        val height = canvas.height
        val delta = 2.0

        canvas.graphicsContext2D.apply {
            clearRect(0.0, delta, width, height) // top
            clearRect(0.0, height - delta, width, height) // bottom
            translate(_dX, _dY)

            val writable = SwingFXUtils.toFXImage(mImage, null)
            drawImage(writable, 0.0, 0.0)
        }
    }

    /**
     * updates the image on repaints the canvas
     * when it receives an update from the Subject
     */
    override fun update(_info: NMEAInfo) {
        repaint()
    }
}