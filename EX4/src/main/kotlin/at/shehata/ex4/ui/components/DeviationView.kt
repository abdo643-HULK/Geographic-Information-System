package at.shehata.ex4.ui.components

import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.NMEAInfo
import at.shehata.ex4.utils.Matrix
import at.shehata.ex4.utils.drawCircle
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Background
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import kotlin.math.sqrt

/**
 * View that shows the clients Latitude and Longitude
 * over time
 */
class DeviationView : Pane(), PositionUpdateListener {
	companion object {
		/**
		 * Size of the time point to draw
		 */
		private const val POINT_SIZE = 8.0
		private const val POINT_SIZE_HALF = POINT_SIZE / 2
	}

	/**
	 * The transformation matrix to make the
	 * points fit onto the screen
	 */
	private var mWorldMatrix = Matrix()

	/**
	 * List of all positions over time
	 */
	private val mPostions = mutableListOf<Point>()

	/**
	 * Diagonal for the Circle
	 */
	private val mDiagonal
		get() = width - 50

	/**
	 * The drawing canvas
	 */
	private val mCanvas = Canvas().apply {
		widthProperty().run {
			bind(this@DeviationView.widthProperty())
			addListener { _, _, _ -> draw() }
		}
		heightProperty().run {
			bind(this@DeviationView.heightProperty())
			addListener { _, _, _ -> draw() }
		}
	}

	init {
		setMinSize(0.0, 0.0)
		background = Background.fill(Color.rgb(211, 211, 211))
		children += mCanvas
	}

	/**
	 * Draws both circles then the line
	 */
	private fun draw() {
		mCanvas.graphicsContext2D.apply {
			lineWidth = 1.0
			clearRect(0.0, 0.0, canvas.width, canvas.height)

			stroke = Color.BLACK
			drawCircle(this, mDiagonal)

			for (i in 0 until mPostions.size - 1) {
				fill = Color.BLACK
				val pt1 = mWorldMatrix * mPostions[i]
				val pt2 = mWorldMatrix * mPostions[i + 1]

				lineWidth = 3.0
				fillOval(pt1.x - POINT_SIZE_HALF, pt1.y - POINT_SIZE_HALF, POINT_SIZE, POINT_SIZE)
				strokeLine(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY())

				fill = Color.RED
				fillOval(pt2.x - POINT_SIZE_HALF, pt2.y - POINT_SIZE_HALF, POINT_SIZE, POINT_SIZE)
			}
		}
	}

	/**
	 * Calculates the bounding box of the points
	 *
	 * @return the bounding box
	 */
	private fun getMapBounds(): Rectangle {
		return when (mPostions.isEmpty()) {
			true -> Rectangle(0, 0, mCanvas.width.toInt(), mCanvas.height.toInt())
			false ->
				Path2D
					.Double()
					.apply {
						mPostions
							.iterator()
							.apply {
								val first = next()
								moveTo(first.getX(), first.getY())
							}
							.forEach { lineTo(it.getX(), it.getY()) }
						closePath()
					}.bounds
		}
	}

	/**
	 * Calculates the transformation matrix
	 * for the points to fit and updates it
	 */
	private fun updateWorldMatrix() {
		val squareWidth = sqrt(2.0) * (mDiagonal / 2)
		val canvasCenterLeft = width / 2 - squareWidth / 2
		val canvasCenterTop = height / 2 - squareWidth / 2
		val rect = Rectangle2D.Double(canvasCenterLeft, canvasCenterTop, squareWidth, squareWidth)

		mWorldMatrix = Matrix.zoomToFit(getMapBounds(), rect.bounds)
	}

	/**
	 * Reads the longitude and latitude and
	 * updates the view to
	 *
	 * @param _info object holding the longitude and latitude
	 */
	override fun update(_info: NMEAInfo) {
		if (_info.mLatitude == null || _info.mLongitude == null) return
		mPostions.add(Point((_info.mLongitude!! * 10000000).toInt(), (_info.mLatitude!! * 10000000).toInt()))
		updateWorldMatrix()
		draw()
	}
}
