package at.shehata.ex2.gis

import at.shehata.ex2.GISApplication
import at.shehata.ex2.gis.components.BottomBar
import at.shehata.ex2.gis.components.ButtonActions
import at.shehata.ex2.gis.components.CanvasPane
import at.shehata.ex2.gis.components.MenuBar
import at.shehata.ex2.interfaces.IDataObserver
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.StackPane
import javafx.embed.swing.SwingFXUtils
import java.awt.Image
import java.awt.image.BufferedImage

/**
 * Our View that gets Attached to the Scene.
 * Is also an observer
 *
 * @param mController the controller with the handlers for the elements
 */
class GISView(private val mController: GISController) : IDataObserver, BorderPane() {
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
        top = MenuBar()
        center = CanvasPane(
            mController.getMouseHandler(),
            mController.getChangeHandler(),
        )
        bottom = BottomBar(mController.getActionHandler())
        onKeyPressed = mController.getKeyHandler()
        onKeyReleased = mController.getKeyHandler()
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

    /**
     * updates the image on repaints the canvas
     * when it receives an update from the Subject
     */
    override fun update(_img: Image) {
        mImage = _img as BufferedImage
        repaint()
    }

}