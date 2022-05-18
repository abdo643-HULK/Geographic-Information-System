package at.shehata.ex3.client.gis

import at.shehata.ex3.client.interfaces.IDataObserver
import at.shehata.ex3.server.OSMServer
import at.shehata.ex3.utils.GeoObject
import at.shehata.ex3.utils.Matrix
import at.shehata.ex3.utils.POIObject
import at.shehata.ex3.utils.POITypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.*
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

private const val INCHES_PER_CENTIMETER = 2.54
private const val POI_IMAGE_HEIGHT = 28
private const val POI_IMAGE_WIDTH = 20

/**
 * Contains the core logic that the controller calls
 */
open class GISModel {
    private var mWorldMatrix = Matrix()

    private val mPOIsImage = BufferedImage(POI_IMAGE_WIDTH, POI_IMAGE_HEIGHT, BufferedImage.TRANSLUCENT).apply {
        val img = ImageIO.read(this@GISModel.javaClass.getResource("/POI.png"))
        createGraphics().apply {
            drawImage(img, 0, 0, POI_IMAGE_WIDTH, POI_IMAGE_HEIGHT, null)
            dispose()
        }
    }

    /**
     * List of all Polygons to render
     */
    private val mData = mutableListOf<GeoObject>()

    private val mPOIData = mutableListOf<POIObject>()

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

    suspend fun loadData() = withContext(Dispatchers.Default) {
//        val stmt = "SELECT * FROM data WHERE type in (233, 931, 932, 933, 934, 1101)"
//        DummyGIS().apply {
//            if (init()) {
//                extractData(stmt)?.let { mData += it.toTypedArray() }
//            }
//        }
//        mData += VerwaltungsgrenzenServer().load3857Data()
        mData.clear()
        mData += OSMServer().loadData().flatten()
    }

    suspend fun loadPOIData() = withContext(Dispatchers.Default) {
        mPOIData += mutableListOf(
            POIObject("0", POITypes.MOSQUE, Polygon(intArrayOf(197), intArrayOf(262), 1)),
            POIObject("1", POITypes.POST, Polygon(intArrayOf(349), intArrayOf(308), 1)),
            POIObject("2", POITypes.PUB, Polygon(intArrayOf(258), intArrayOf(149), 1)),
            POIObject("3", POITypes.SCHOOL, Polygon(intArrayOf(363), intArrayOf(203), 1)),
            POIObject("4", POITypes.SHOP, Polygon(intArrayOf(376), intArrayOf(255), 1)),
        )
    }

    suspend fun toggleSticky() = withContext(Dispatchers.Default) {
        mData.clear()
        mData += OSMServer().getArea(mWorldMatrix.inverse() * Rectangle(0, 0, mWidth, mHeight))
    }

    fun hidePOI() {
        mImage = initCanvas()
        mPOIData.clear()
        repaint()
    }

    /**
     * Paints the polygons onto the Image and
     * calls the observers.
     */
    fun repaint() {
//        val context = DummyGIS().getDrawingContext()
//        val context = VerwaltungsgrenzenServer().getDrawingContext()
        val context = OSMServer().drawingContext
        mImage.graphics.let { graphics ->
            val gc = graphics as Graphics2D
            graphics.color = Color.BLACK
            graphics.fillRect(0, 0, mWidth, mHeight)
            mData.forEach {
                val values = context.getSchema(it.mType) ?: context.getDefaultSchema()
                values.paint(gc, it, mWorldMatrix)
            }
            mPOIData.forEach {
                gc.drawImage(mPOIsImage, it.mPolygons[0].xpoints[0], it.mPolygons[0].ypoints[0], null)
            }
        }
        update(mImage)
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
     */
    fun zoom(_pt: Point, _factor: Double) {
        mImage = initCanvas()
        mWorldMatrix = Matrix.zoomPoint(mWorldMatrix, _pt, _factor)
        repaint()
    }

    suspend fun zoomNonBlock(_pt: Point, _factor: Double) = withContext(Dispatchers.Default) {
        zoom(_pt, _factor)
    }

    /**
     * Veraendert die interne Transformationsmatrix so, dass in das
     * Zentrum des Anzeigebereiches herein- bzw. herausgezoomt wird
     *
     * @param _factor Der Faktor um den herein- bzw. herausgezoomt wird
     */
    fun zoom(_factor: Double) {
        mImage = initCanvas()
        mWorldMatrix = Matrix.zoomPoint(mWorldMatrix, Point(mWidth / 2, mHeight / 2), _factor)
        repaint()
    }

    suspend fun zoomNonBlock(_factor: Double) = withContext(Dispatchers.Default) {
        zoom(_factor)
    }

    /**
     * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
     * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
     * komplett in den Anzeigebereich passen
     */
    fun zoomToFit() {
        mImage = initCanvas()
        mWorldMatrix = Matrix.zoomToFit(
            getMapBounds(mData.map { it.mPolygons }),
            Rectangle(0, 0, mWidth, mHeight)
        )
        repaint()
    }

    suspend fun zoomToFitNonBlock() = withContext(Dispatchers.Default) {
        zoomToFit()
    }

    fun zoomTo(_rec: Rectangle) {
        mImage = initCanvas()
        mWorldMatrix = Matrix.zoomToFit(mWorldMatrix.inverse() * _rec, Rectangle(0, 0, mWidth, mHeight))
        repaint()
    }

    suspend fun zoomToNonBlock(_rec: Rectangle) = withContext(Dispatchers.Default) {
        zoomTo(_rec)
    }

    /**
     * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
     * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
     * komplett in den Anzeigebereich passen
     */
    fun zoomToScale(_scale: Int) {
        mImage = initCanvas()
        val vector = Point2D.Double(0.0, 1.0)

        val a = mDotPerInch / INCHES_PER_CENTIMETER
        val b = vector.distance(0.0, 0.0)
        val c = (a * b / _scale)

        mWorldMatrix = Matrix.zoomPoint(
            mWorldMatrix,
            Point(mWidth / 2, mHeight / 2),
            calculateScale() / _scale.toDouble()
        )

        repaint()
    }

    suspend fun zoomToScaleNonBlock(_scale: Int) = withContext(Dispatchers.Default) {
        zoomToScale(_scale)
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
    }

    fun rotate(_alpha: Double) {
        val centerX = mWidth / 2.0
        val centerY = mHeight / 2.0
        mImage = initCanvas()
        mWorldMatrix = Matrix.translate(centerX, centerY) *
                Matrix.rotate(_alpha) *
                Matrix.translate(-centerX, -centerY) *
                mWorldMatrix
        repaint()
    }


    /**
     * Ermittelt die gemeinsame BoundingBox der übergebenen Polygone
     *
     * @param _poly Die Polygone, für die die BoundingBox berechnet
     * werden soll
     * @return Die BoundingBox
     */
    fun getMapBounds(_poly: List<List<Polygon>>): Rectangle {
        if (_poly.isEmpty()) return Rectangle(0, 0, mWidth, mHeight)
        val boundingBox = Rectangle(_poly[0][0].bounds).apply {
            _poly[0].drop(1).forEach { add(it.bounds) }
        }

        for (i in (1 until _poly.size)) {
            _poly[i].forEach {
                boundingBox.add(it.bounds)
            }
        }

        return boundingBox
    }

    suspend fun getMapBoundsNonBlocking(_poly: List<List<Polygon>>): Rectangle = withContext(Dispatchers.Default) {
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
     * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
     * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
     * innerhalb eines definierten Rechtecks (_winBounds) komplett in den
     * Anzeigebereich (die Zeichenflaeche) passen
     *
     * @param _mapBounds Der darzustellende Bereich in Bildschirm-Koordinaten
     */
    fun zoomRect(_mapBounds: Rectangle) {

    }

    /**
     * Liefert zu einem Punkt im Bildschirmkoordinatensystem den passenden
     * Punkt im Kartenkoordinatensystem
     *
     * @param _pt Der umzuwandelnde Punkt im Bildschirmkoordinatensystem
     * @return Der gleiche Punkt im Weltkoordinatensystem
     * @see java.awt.Point
     */
    fun getMapPoint(_pt: Point): Point {
        return Point()
    }
}