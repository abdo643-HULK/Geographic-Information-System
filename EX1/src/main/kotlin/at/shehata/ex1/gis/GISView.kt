package at.shehata.ex1.gis

import at.shehata.ex1.GISApplication
import at.shehata.ex1.interfaces.IDataObserver
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.StackPane
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventType
import java.awt.Image
import java.awt.Polygon
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

        val buttonPane = FlowPane()
        buttonPane.style = "-fx-background-color: blue"
        bottom = buttonPane
        val buttonPaneBtn = Button("Click")
        buttonPaneBtn.style = "-fx-padding: 5"
        buttonPaneBtn.onAction = mController.getActionHandler()
        buttonPane.children.add(buttonPaneBtn)

        val canvasPane = StackPane()
        canvasPane.style = "-fx-background-color: red"
        canvasPane.setMinSize(0.0, 0.0)
        center = canvasPane
        val canvas = Canvas()
        canvas.id = GISApplication.CANVAS_ID
        canvas.widthProperty().bind(canvasPane.widthProperty())
        canvas.heightProperty().bind(canvasPane.heightProperty())
        canvas.widthProperty().addListener(mController.getChangeHandler())
        canvas.heightProperty().addListener(mController.getChangeHandler())
        canvasPane.children.add(canvas)

        onMouseReleased = mController.getMouseHandler()
    }

    /**
     * renders the image on the canvas
     */
    fun repaint() {
        val writable = SwingFXUtils.toFXImage(mImage, null)
        val c = scene.lookup("#${GISApplication.CANVAS_ID}") as Canvas
        val gc = c.graphicsContext2D
        gc.clearRect(0.0, 0.0, c.width, c.height)
        gc.drawImage(writable, 0.0, 0.0)
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