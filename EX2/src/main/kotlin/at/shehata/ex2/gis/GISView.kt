package at.shehata.ex2.gis

import at.shehata.ex2.GISApplication
import at.shehata.ex2.gis.components.ButtonActions
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

        val buttonPaneBtn = Button("Draw").apply {
            id = ButtonActions.DRAW.name
            style = "-fx-padding: 5"
            onAction = mController.getActionHandler()
        }
        val buttonPane = FlowPane().apply {
            style = "-fx-background-color: blue"
            children += buttonPaneBtn
        }
        bottom = buttonPane

        val canvasPane = StackPane().apply {
            setMinSize(0.0, 0.0)
            style = "-fx-background-color: red"
            onMouseReleased = mController.getMouseHandler()
            widthProperty().addListener(mController.getChangeHandler())
            heightProperty().addListener(mController.getChangeHandler())
        }
        val canvas = Canvas().apply {
            id = GISApplication.CANVAS_ID
            widthProperty().bind(canvasPane.widthProperty())
            heightProperty().bind(canvasPane.heightProperty())
        }
        canvasPane.children += canvas
        center = canvasPane
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