package at.shehata.ex4.model

import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.utils.GeoObject
import at.shehata.ex4.utils.Matrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle
import java.awt.geom.Point2D
import java.awt.image.BufferedImage

private const val INCHES_PER_CENTIMETER = 2.54

/**
 * Contains the core logic that the controller calls
 */
open class GISModel {
	private val mMutex = Mutex()

	private var mWorldMatrix = Matrix()

	/**
	 * List of all Polygons to render
	 */
	private val mData = mutableListOf<GeoObject>()

	private val mDotPerInch = 72.0

	/**
	 * width and height of the image
	 */
	private var mWidth: Int = 1
	private var mHeight: Int = 1

	/**
	 * The image that gets filled with polygons
	 */
	private lateinit var mImage: Image

	/**
	 * The observer that gets called on updates
	 */
	private lateinit var mObserver: PositionUpdateListener


	/**
	 * Creates an Image that gets rendered on the canvas
	 */
	fun initCanvas(): Image = BufferedImage(mWidth, mHeight, BufferedImage.TYPE_INT_RGB)

	/**
	 * sets the width of the image and repaints
	 * the canvas
	 *
	 * @param _width the new width
	 */
	fun setWidth(_width: Int) {
		mWidth = _width
		zoomToFit()
	}

	/**
	 * sets the height of the image and repaints
	 * the canvas
	 *
	 * @param _height the new height
	 */
	fun setHeight(_height: Int) {
		mHeight = _height
		zoomToFit()
	}

	suspend fun loadData() = withContext(Dispatchers.IO) {
		mData.clear()
	}


	suspend fun hidePOI() {
		mImage = initCanvas()
		repaint()
	}

	/**
	 * Paints the polygons onto the Image and
	 * calls the observers.
	 */
	suspend fun repaint() = withContext(Dispatchers.Default) {
		mImage.graphics.let { graphics ->
			graphics as Graphics2D
			graphics.color = Color(240, 240, 240)
			graphics.fillRect(0, 0, mWidth, mHeight)

			mMutex.withLock {
				mData.forEach {
//					val values = context.getSchema(it.mType) ?: context.getDefaultSchema()
//					values.paint(graphics, it, mWorldMatrix)
				}
			}
		}

		withContext(Dispatchers.Main) {
			update(mImage)
		}
	}

	/**
	 * Calls the observer with the new image (to paint)
	 *
	 * @param _house the updated image
	 */
	protected open fun update(_house: Image) {
		mObserver.update(_house, calculateScale())
	}

	/**
	 * Updates the Observer
	 */
	fun addMapObserver(_observer: PositionUpdateListener) {
		mObserver = _observer
	}

	/**
	 * Berechnet den gerade sichtbaren Massstab der Karte
	 *
	 * @return der Darstellungsmassstab
	 * @see Matrix
	 */
	protected fun calculateScale(): Int {
		val vector = Point2D.Double(0.0, 1.0)
		val vectorTransformed = (mWorldMatrix * vector)

		val a = mDotPerInch / INCHES_PER_CENTIMETER
		val b = vector.distance(0.0, 0.0)
		val c = vectorTransformed.distance(0.0, 0.0)

		return (a * b / c).toInt()
	}

	/**
	 * Veraendert die interne Transformationsmatrix so, dass an dem
	 * uebergebenen Punkt herein- bzw. herausgezoomt wird
	 *
	 * @param _pt Der Punkt an dem herein- bzw. herausgezoomt wird
	 * @param _factor Der Faktor um den herein- bzw. herausgezoomt wird
	 *
	 * @see java.awt.Point
	 */
	fun zoom(_pt: java.awt.Point, _factor: Double) {
		mImage = initCanvas()
		mWorldMatrix = Matrix.zoomPoint(mWorldMatrix, _pt, _factor)
	}

	suspend fun zoomNonBlock(_pt: java.awt.Point, _factor: Double) = withContext(Dispatchers.Default) {
		zoom(_pt, _factor)
		repaint()
	}

	/**
	 * Veraendert die interne Transformationsmatrix so, dass in das
	 * Zentrum des Anzeigebereiches herein- bzw. herausgezoomt wird
	 *
	 * @param _factor Der Faktor um den herein- bzw. herausgezoomt wird
	 */
	fun zoom(_factor: Double) {
		mImage = initCanvas()
		mWorldMatrix = Matrix.zoomPoint(mWorldMatrix, java.awt.Point(mWidth / 2, mHeight / 2), _factor)
	}

	suspend fun zoomNonBlock(_factor: Double) = withContext(Dispatchers.Default) {
		zoom(_factor)
		repaint()
	}

	/**
	 * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
	 * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
	 * komplett in den Anzeigebereich passen
	 */
	fun zoomToFit() {
		mImage = initCanvas()
		mWorldMatrix = Matrix.zoomToFit(
			getMapBounds(mData),
			Rectangle(0, 0, mWidth, mHeight)
		)
	}

	suspend fun zoomToFitNonBlock() = withContext(Dispatchers.Default) {
		zoomToFit()
		repaint()
	}

	/**
	 * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
	 * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
	 * innerhalb eines definierten Rechtecks (_winBounds) komplett in den
	 * Anzeigebereich (die Zeichenflaeche) passen
	 *
	 * @param _mapBounds Der darzustellende Bereich in Bildschirm-Koordinaten
	 */
	fun zoomRect(_mapBounds: Rectangle) {
		mImage = initCanvas()
		mWorldMatrix = Matrix.zoomToFit(mWorldMatrix.inverse() * _mapBounds, Rectangle(0, 0, mWidth, mHeight))
	}

	suspend fun zoomRectNonBlock(_mapBounds: Rectangle) = withContext(Dispatchers.Default) {
		zoomRect(_mapBounds)
		repaint()
	}

	/**
	 * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
	 * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
	 * komplett in den Anzeigebereich passen
	 */
	fun zoomToScale(_scale: Int) {
		mImage = initCanvas()
		mWorldMatrix = Matrix.zoomPoint(
			mWorldMatrix,
			java.awt.Point(mWidth / 2, mHeight / 2),
			calculateScale() / _scale.toDouble()
		)
	}

	suspend fun zoomToScaleNonBlock(_scale: Int) = withContext(Dispatchers.Default) {
		zoomToScale(_scale)
		repaint()
	}

	/**
	 * Veraendert die interne Transformationsmatrix so, dass
	 * die zu zeichnenden Objekt horizontal verschoben werden.
	 *
	 * @param _delta Die Strecke, um die horizontal verschoben werden soll
	 */
	fun scrollHorizontal(_delta: Int) {
		mImage = initCanvas()
		mWorldMatrix = Matrix.translate(_delta.toDouble(), 0.0) * mWorldMatrix
	}

	suspend fun scrollHorizontalNonBlocking(_delta: Int) = withContext(Dispatchers.Default) {
		scrollHorizontal(_delta)
		repaint()
	}

	/**
	 * Veraendert die interne Transformationsmatrix so, dass
	 * die zu zeichnenden Objekt vertikal verschoben werden.
	 *
	 * @param _delta Die Strecke, um die vertikal verschoben werden soll
	 */
	fun scrollVertical(_delta: Int) {
		mImage = initCanvas()
		mWorldMatrix = Matrix.translate(0.0, _delta.toDouble()) * mWorldMatrix
	}

	suspend fun scrollVerticalNonBlocking(_delta: Int) = withContext(Dispatchers.Default) {
		scrollVertical(_delta)
		repaint()
	}

	fun rotate(_alpha: Double) {
		val centerX = mWidth / 2.0
		val centerY = mHeight / 2.0
		mImage = initCanvas()
		mWorldMatrix = Matrix.translate(centerX, centerY) *
				Matrix.rotate(_alpha) *
				Matrix.translate(-centerX, -centerY) *
				mWorldMatrix
	}

	suspend fun rotateNonBlocking(_alpha: Double) = withContext(Dispatchers.Default) {
		rotate(_alpha)
		repaint()
	}


	/**
	 * Ermittelt die gemeinsame BoundingBox der übergebenen Polygone
	 *
	 * @param _geoObj Die Polygone, für die die BoundingBox berechnet
	 * werden soll
	 * @return Die BoundingBox
	 */
	fun getMapBounds(_geoObj: List<GeoObject>): Rectangle {
		if (_geoObj.isEmpty()) return Rectangle(0, 0, mWidth, mHeight)
		val boundingBox = _geoObj[0].mBounds[0].apply {
			_geoObj.drop(1).forEach { g ->
				g.mBounds.forEach { add(it) }
			}
		}

//		_geoObj.forEach { g ->
//			g.mBounds.forEach { boundingBox.add(it) }
//		}

		return boundingBox
	}

	suspend fun getMapBoundsNonBlocking(_poly: List<GeoObject>): Rectangle = withContext(Dispatchers.Default) {
		getMapBounds(_poly)
	}

	/**
	 * Ermittelt die Geo-Objekte, die den Punkt (in Bildschirmkoordinaten)
	 * enthalten
	 *
	 * @param _pt Ein Selektionspunkt im Bildschirmkoordinatensystem
	 * @return Eine Liste von Geo-Objekte, die den Punkt enthalten
	 * @see java.awt.Point
	 * @see GeoObject
	 */
	fun initSelection(_pt: at.shehata.ex4.utils.Point): List<GeoObject> {
		return listOf()
	}


	/**
	 * Liefert zu einem Punkt im Bildschirmkoordinatensystem den passenden
	 * Punkt im Kartenkoordinatensystem
	 *
	 * @param _pt Der umzuwandelnde Punkt im Bildschirmkoordinatensystem
	 * @return Der gleiche Punkt im Weltkoordinatensystem
	 * @see java.awt.Point
	 */
	fun getMapPoint(_pt: java.awt.Point) = mWorldMatrix.inverse() * _pt
}