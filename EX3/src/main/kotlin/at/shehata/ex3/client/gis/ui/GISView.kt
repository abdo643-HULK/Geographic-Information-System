package at.shehata.ex3.client.gis.ui

import at.shehata.ex3.CANVAS_ID
import at.shehata.ex3.OVERLAY_ID
import at.shehata.ex3.client.gis.controller.GISController
import at.shehata.ex3.client.gis.ui.components.BottomBar
import at.shehata.ex3.client.gis.ui.components.CanvasPane
import at.shehata.ex3.client.gis.ui.components.MenuBar
import at.shehata.ex3.client.gis.ui.components.SCALE_FIELD_ID
import at.shehata.ex3.client.interfaces.IDataObserver
import javafx.embed.swing.SwingFXUtils
import javafx.scene.canvas.Canvas
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import java.awt.Image
import java.awt.geom.Rectangle2D
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
		top = MenuBar(mController.getActionHandler())
		center = CanvasPane(
			mController.getMouseHandler(),
			mController.getChangeHandler(),
		)
		bottom = BottomBar(mController.getActionHandler(), mController.getKeyHandler())
		onKeyPressed = mController.getKeyHandler()
		onKeyReleased = mController.getKeyHandler()
		onScroll = mController.getScrollHandler()
	}

	/**
	 * renders the image on the canvas
	 */
	fun repaint() {
		val writable = SwingFXUtils.toFXImage(mImage, null)
		val canvas = scene.lookup("#${CANVAS_ID}") as Canvas

		canvas.graphicsContext2D.apply {
			clearRect(0.0, 0.0, canvas.width, canvas.height)
			drawImage(writable, 0.0, 0.0)
		}
	}

	/**
	 * Draws a Rectangle on the overlay canvas
	 *
	 * @param _rect The rectangle to render
	 */
	fun drawXOR(_rect: Rectangle2D) {
		val overlay = scene.lookup("#${OVERLAY_ID}") as Canvas
		overlay.graphicsContext2D.apply {
			clearRect(0.0, 0.0, scene.width, scene.height)
			stroke = Color.AZURE
			lineWidth = 2.0
			strokeRect(_rect.minX, _rect.minY, _rect.width, _rect.height)
		}
	}

	/**
	 * Clears the overlay canvas
	 */
	fun clearXOR() {
		val overlay = scene.lookup("#${OVERLAY_ID}") as Canvas
		overlay.graphicsContext2D.apply {
			clearRect(0.0, 0.0, scene.width, scene.height)
		}
	}

	/**
	 * Saves the context of the rendering canvas
	 */
	fun saveContext() {
		val canvas = scene.lookup("#${CANVAS_ID}") as Canvas
		canvas.graphicsContext2D.save()
	}

	/**
	 * Restores the context of the rendering canvas
	 */
	fun restoreContext() {
		val canvas = scene.lookup("#${CANVAS_ID}") as Canvas
		canvas.graphicsContext2D.restore()
	}

	/**
	 * Translates the rendering canvas by the giving delta in both direction
	 *
	 * @param _dX The amount to translate in X direction
	 * @param _dY The amount to translate in Y direction
	 */
	fun translate(_dX: Double, _dY: Double) {
		val canvas = scene.lookup("#${CANVAS_ID}") as Canvas
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
	 * Updates the image and repaints the canvas
	 * when it receives an update from the Subject.
	 * It also updates the scale TextField
	 */
	override fun update(_img: Image, _scale: Int) {
		mImage = _img as BufferedImage
		(scene.lookup("#$SCALE_FIELD_ID") as TextField).apply {
			text = "$_scale"
		}
		repaint()
	}

}