package at.shehata.ex3.client.gis

import at.shehata.ex3.client.gis.components.Server
import at.shehata.ex3.client.interfaces.IDataObserver
import at.shehata.ex3.server.DummyGIS
import at.shehata.ex3.server.OSMServer
import at.shehata.ex3.server.VerwaltungsgrenzenServer
import at.shehata.ex3.utils.*
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
import javax.imageio.ImageIO
import at.shehata.ex3.server.interfaces.Server as IServer
import java.awt.Point as AwtPoint

private const val INCHES_PER_CENTIMETER = 2.54
private const val POI_IMAGE_HEIGHT = 28
private const val POI_IMAGE_WIDTH = 20

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

	private val mPOIData = mutableListOf<POIObject>()

	private val mDotPerInch = 72.0

	private val mPOIsImage = BufferedImage(POI_IMAGE_WIDTH, POI_IMAGE_HEIGHT, BufferedImage.TRANSLUCENT).apply {
		val img = ImageIO.read(this@GISModel.javaClass.getResource("/POI.png"))
		createGraphics().apply {
			drawImage(img, 0, 0, POI_IMAGE_WIDTH, POI_IMAGE_HEIGHT, null)
			dispose()
		}
	}

	/**
	 * width and height of the image
	 */
	private var mWidth: Int = 1
	private var mHeight: Int = 1
	private var mServer: IServer = OSMServer()

	/**
	 * The image that gets filled with polygons
	 */
	private lateinit var mImage: Image

	/**
	 * The observer that gets called on updates
	 */
	private lateinit var mObserver: IDataObserver


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

	fun setServer(_server: Server) {
		mServer = when (_server) {
			Server.OSM -> OSMServer()
			Server.DUMMY_GIS -> DummyGIS()
			Server.VERWALTUNGSGRENZEN -> VerwaltungsgrenzenServer()
		}
	}

	suspend fun loadData() = withContext(Dispatchers.IO) {
		mData.clear()
		mData += mServer.loadData()
	}

	suspend fun loadPOIData() = withContext(Dispatchers.Default) {
		mPOIData += mutableListOf(
			POIObject("0", POITypes.MOSQUE, Point(AwtPoint(1616032, 6168480))),
			POIObject("1", POITypes.POST, Point(AwtPoint(1615736, 6168334))),
			POIObject("2", POITypes.PUB, Point(AwtPoint(1615844, 6169376))),
			POIObject("3", POITypes.SCHOOL, Point(AwtPoint(1615775, 6167628))),
			POIObject("4", POITypes.SHOP, Point(AwtPoint(1615211, 6167746))),
		)
	}

	suspend fun loadAreaData() = withContext(Dispatchers.Default) {
		mData.clear()
		mData += mServer.getArea(mWorldMatrix.inverse() * Rectangle(0, 0, mWidth, mHeight))
	}

	suspend fun hidePOI() {
		mImage = initCanvas()
		mPOIData.clear()
		repaint()
	}

	/**
	 * Paints the polygons onto the Image and
	 * calls the observers.
	 */
	suspend fun repaint() = withContext(Dispatchers.Default) {
		val context = mServer.mDrawingContext
		mImage.graphics.let { graphics ->
			graphics as Graphics2D
			graphics.color = Color(240, 240, 240)
			graphics.fillRect(0, 0, mWidth, mHeight)

			mMutex.withLock {
				mData.forEach {
					val values = context.getSchema(it.mType) ?: context.getDefaultSchema()
					values.paint(graphics, it, mWorldMatrix)
				}

				mPOIData.forEach {
					val pt = (it.mObjects[0] as Point).mGeometry
					val pos = mWorldMatrix * AwtPoint(pt.x, pt.y)
					graphics.drawImage(mPOIsImage, pos.x, pos.y, null)
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
	fun addMapObserver(_observer: IDataObserver) {
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
	fun zoom(_pt: AwtPoint, _factor: Double) {
		mImage = initCanvas()
		mWorldMatrix = Matrix.zoomPoint(mWorldMatrix, _pt, _factor)
	}

	suspend fun zoomNonBlock(_pt: AwtPoint, _factor: Double) = withContext(Dispatchers.Default) {
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
		mWorldMatrix = Matrix.zoomPoint(mWorldMatrix, AwtPoint(mWidth / 2, mHeight / 2), _factor)
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
			AwtPoint(mWidth / 2, mHeight / 2),
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
	fun initSelection(_pt: Point): List<GeoObject> {
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
	fun getMapPoint(_pt: AwtPoint) = mWorldMatrix.inverse() * _pt
}