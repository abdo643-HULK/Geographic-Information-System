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


class GISView(private val mController: GISController) : IDataObserver, BorderPane() {

    private lateinit var mImage: BufferedImage

    private val mScene by lazy { scene }

    init {
        start()
    }

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


//    fun repaint() {
//        val x: DoubleArray = Arrays.stream(mPolygon.xpoints).asDoubleStream().toArray()
//        val y: DoubleArray = Arrays.stream(mPolygon.ypoints).asDoubleStream().toArray()
//        val c = mScene.lookup("#" + GISApplication.CANVAS_ID) as Canvas
//        val gc = c.graphicsContext2D
//        gc.strokePolygon(x, y, mPolygon.npoints)
//    }

    fun repaint() {
        val writable = SwingFXUtils.toFXImage(mImage, null)
        val c = mScene.lookup("#${GISApplication.CANVAS_ID}") as Canvas
        val gc = c.graphicsContext2D
        gc.clearRect(0.0, 0.0, c.width, c.height)
        gc.drawImage(writable, 0.0, 0.0)
    }

    override fun update(_img: Image) {
        mImage = _img as BufferedImage
        repaint()
    }

}