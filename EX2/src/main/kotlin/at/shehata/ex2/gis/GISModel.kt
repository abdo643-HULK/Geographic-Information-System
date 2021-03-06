package at.shehata.ex2.gis

import at.shehata.ex2.interfaces.IDataObserver
import at.shehata.ex2.utils.GeoObject
import at.shehata.ex2.utils.Matrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.util.*

/**
 * Contains the core logic that the controller calls
 */
open class GISModel {
    /**
     * The Transformation Matrix for our DrawSpace/World
     */
    private var mWorldMatrix = Matrix()

    /**
     * List of all GeoObjects to render
     */
    private val mData = mutableListOf<GeoObject>()

    /**
     * Width of the image
     */
    private var mWidth: Int = 1

    /**
     * Height of the image
     */
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
     * Calls the Server for the GeoObjects and adds
     * them to list
     */
    suspend fun loadData() = withContext(Dispatchers.Default) {
        val geoObjects = DummyGIS().let {
            if (it.init()) {
//                it.extractData("select * from data where type = 1101")
                it.extractData("SELECT * FROM data WHERE type in (233, 931, 932, 933, 934, 1101)")
            } else {
                null
            }
        }

//        geoObjects?.let { polygon ->
//            val stringArray = polygon.joinToString(",\n") { "{ \"npoints\": ${it.npoints}, \"xpoints\": ${it.xpoints.contentToString()}, \"ypoints\": ${it.ypoints.contentToString()} }" }
//            File("./data.json").writeText("{\n \"data\": [$stringArray] \n}");
//        }
        geoObjects?.let { mData.addAll(it.toTypedArray()) }
    }

    /**
     * Load the Data from the Server and
     * paints them
     */
    suspend fun drawData() {
        loadData()
        repaint()
    }

    /**
     * Creates an Image that gets rendered on the canvas
     */
    fun initCanvas(): Image = BufferedImage(mWidth, mHeight, BufferedImage.TYPE_INT_RGB)

    /**
     * Paints the polygons onto the Image and
     * calls the observers.
     */
    fun repaint() {
        mImage.graphics.color = Color.BLUE
        mData.forEach { mImage.graphics.drawPolygon(mWorldMatrix * it.getPoly()) }
        update(mImage)
    }

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
     * Stellt intern eine Transformationsmatrix zur Verfuegung, die so
     * skaliert, verschiebt und spiegelt, dass die zu zeichnenden Polygone
     * komplett in den Anzeigebereich passen
     */
    fun zoomToFit() {
        mImage = initCanvas()
        mWorldMatrix =
            Matrix.zoomToFit(
                getMapBounds(mData.map { it.getPoly() }),
                Rectangle(0, 0, mWidth, mHeight - 5)
            )
        repaint()
    }

    /**
     * Non-blocking Version of zoomToFit
     * @see at.shehata.ex2.gis.GISModel.zoomToFit
     */
    suspend fun zoomToFitNonBlock() = withContext(Dispatchers.Default) {
        zoomToFit()
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

    /**
     * Non-blocking Version of zoom
     *
     * @see at.shehata.ex2.gis.GISModel.zoom
     */
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

    /**
     * Non-blocking Version of zoom
     *
     * @see at.shehata.ex2.gis.GISModel.zoom
     */
    suspend fun zoomNonBlock(_factor: Double) = withContext(Dispatchers.Default) {
        zoom(_factor)
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

    /**
     * Non-blocking Version of getMapBounds
     *
     * @see at.shehata.ex2.gis.GISModel.getMapBounds
     */
    suspend fun getMapBoundsNonBlocking(_poly: Vector<Polygon>): Rectangle = withContext(Dispatchers.Default) {
        getMapBounds(_poly)
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

    /**
     * Non-blocking Version of scrollHorizontal
     *
     * @see at.shehata.ex2.gis.GISModel.scrollHorizontal
     */
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

    /**
     * Non-blocking Version of scrollVertical
     *
     * @see at.shehata.ex2.gis.GISModel.scrollVertical
     */
    suspend fun scrollVerticalNonBlocking(_delta: Int) = withContext(Dispatchers.Default) {
        scrollVertical(_delta)
    }

    /**
     * Changes the internal transformation Matrix so
     * that all objects are rotated.
     *
     * @param _alpha The angle to rotate with in radians
     */
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
}