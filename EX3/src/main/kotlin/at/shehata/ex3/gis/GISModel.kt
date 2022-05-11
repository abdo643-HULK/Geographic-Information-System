package at.shehata.ex3.gis

import at.shehata.ex3.interfaces.IDataObserver
import at.shehata.ex3.utils.GeoObject
import at.shehata.ex3.utils.Matrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.*
import java.awt.image.BufferedImage
import java.util.*

/**
 * Contains the core logic that the controller calls
 */
open class GISModel {
    private var mWorldMatrix = Matrix()

    /**
     * List of all Polygons to render
     */
    private val mData = mutableListOf<GeoObject>()

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
//        mImage = initCanvas()
//        repaint()
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
//        mImage = initCanvas()
//        repaint()
        zoomToFit()
    }


    suspend fun loadData() = withContext(Dispatchers.Default) {
        val stmt = "SELECT * FROM data WHERE type in (233, 931, 932, 933, 934, 1101)"
        DummyGIS().apply {
            if (init()) {
                extractData(stmt)?.let { mData.addAll(it.toTypedArray()) }
            }
        }
    }


    suspend fun drawData() {
        loadData()
        repaint()
    }

    /**
     * Paints the polygons onto the Image and
     * calls the observers.
     */
    fun repaint() {
        val context = DummyGIS().getDrawingContext()
        mImage.graphics.let { graphics ->
            graphics.color = Color.BLACK
            graphics.fillRect(0, 0, mWidth, mHeight)
            mData.forEach {
                val values = context.getSchema(it.getType()) ?: context.getDefaultSchema()
                values.paint(graphics as Graphics2D, it, mWorldMatrix)
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
        mObserver.update(_house)
    }

    /**
     * Updates the Observer
     */
    fun addMapObserver(_observer: IDataObserver) {
        mObserver = _observer
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
            getMapBounds(mData.map { it.getPoly() }),
            Rectangle(0, 0, mWidth, mHeight - 5)
        )
        repaint()
    }

    suspend fun zoomToFitNonBlock() = withContext(Dispatchers.Default) {
        zoomToFit()
    }

    /**
     * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
     * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
     * komplett in den Anzeigebereich passen
     */
    fun zoomToScale() {
        mImage = initCanvas()
        mWorldMatrix = Matrix.zoomToFit(
            getMapBounds(mData.map { it.getPoly() }),
            Rectangle(0, 0, mWidth, mHeight - 5)
        )
        repaint()
    }

    suspend fun zoomToScaleNonBlock() = withContext(Dispatchers.Default) {
        zoomToScale()
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
        repaint()
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
        repaint()
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
     * Ermittelt die Geo-Objekte, die den Punkt (in Bildschirmkoordinaten)
     * enthalten
     * @param _pt Ein Selektionspunkt im Bildschirmkoordinatensystem
     * @return Ein Vektor von Geo-Objekte, die den Punkt enthalten
     * @see java.awt.Point
     * @see GeoObject
     */
    fun initSelection(_pt: Point): Vector<GeoObject> {
        return Vector()
    }

    /**
     * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
     * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
     * innerhalb eines definierten Rechtecks (_winBounds) komplett in den
     * Anzeigebereich (die Zeichenflaeche) passen
     * @param _mapBounds Der darzustellende Bereich in Bildschirm-Koordinaten
     */
    fun zoomRect(_winBounds: Rectangle) {

    }

    /**
     * Ermittelt die gemeinsame BoundingBox der uebergebenen Polygone
     *
     * @param _poly Die Polygone, fuer die die BoundingBox berechnet
     * werden soll
     * @return Die BoundingBox
     */
    fun getMapBounds(_poly: List<Polygon>): Rectangle {
        if (_poly.isEmpty()) return Rectangle(0, 0, mWidth, mHeight)
        val boundingBox = Rectangle(_poly[0].bounds)

        for (i in (1 until _poly.size)) {
            boundingBox.add(_poly[i].bounds)
        }

        return boundingBox
    }

    suspend fun getMapBoundsNonBlocking(_poly: Vector<Polygon>): Rectangle = withContext(Dispatchers.Default) {
        getMapBounds(_poly)
    }

    /**
     * Liefert zu einem Punkt im Bildschirmkoordinatensystem den passenden
     * Punkt im Kartenkoordinatensystem
     * @param _pt Der umzuwandelnde Punkt im Bildschirmkoordinatensystem
     * @return Der gleiche Punkt im Weltkoordinatensystem
     * @see java.awt.Point
     */
    fun getMapPoint(_pt: Point): Point {
        return Point()
    }
}