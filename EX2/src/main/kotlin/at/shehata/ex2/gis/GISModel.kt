package at.shehata.ex2.gis

import at.shehata.ex2.interfaces.IDataObserver
import de.intergis.JavaClient.JavaClientApplication
import de.intergis.JavaClient.gui.IgcDummyTreeObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.*
import java.awt.image.BufferedImage
import java.util.*

/**
 * Contains the core logic that the controller calls
 */
class GISModel {
    /**
     * List of all Polygons to render
     */
    private val mData = mutableListOf<Polygon>()

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
     * Generates a house at a random position inside the canvas
     */
    fun generateRndHome() {
        val deltaX = (0..mWidth - 40).random()
        val deltaY = (0..mHeight - 50).random()
        val x = intArrayOf(10, 40, 40, 25, 10, 10).map { deltaX + it }.toIntArray()
        val y = intArrayOf(10, 10, 30, 50, 30, 10).map { deltaY + it }.toIntArray()
        mData.add(Polygon(x, y, x.size))
        repaint()
    }

    /**
     * Generates a house at the supplied position inside the canvas
     *
     * @param _x the x coordinate
     * @param _y the x coordinate
     */
    fun generateHome(_x: Int, _y: Int) {
        val startX = _x - 10
        val startY = _y - 10
        val x = intArrayOf(startX + 10, startX + 40, startX + 40, startX + 25, startX + 10, startX + 10)
        val y = intArrayOf(startY + 10, startY + 10, startY + 30, startY + 50, startY + 30, startY + 10)
        mData.add(Polygon(x, y, x.size))
        repaint()
    }

    fun drawBigHouse() {
        val poly = Polygon().apply {
            addPoint(10000, 10000)
            addPoint(70000, 10000)
            addPoint(70000, 70000)
            addPoint(40000, 90000)
            addPoint(10000, 70000)
            addPoint(10000, 10000)
        }

        mData.add(poly)
        repaint()
    }

    fun loadData() {
        val objects = DummyGIS().let {
            if (it.init()) {
                it.extractData("select * from data where type = 1101")
            } else {
                null
//                throw Error("Unable to connect to Server")
            }
        }
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
        mData.forEach { mImage.graphics.fillPolygon(it) }
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
        mImage = initCanvas()
        repaint()
    }

    /**
     * sets the height of the image and repaints
     * the canvas
     *
     * @param _height the new height
     */
    fun setHeight(_height: Int) {
        mHeight = _height
        mImage = initCanvas()
        repaint()
    }

    /**
     * Calls the observer with the new image (to paint)
     *
     * @param _house the updated image
     */
    protected fun update(_house: Image) {
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
    fun zoomToFit() {}

    suspend fun zoomToFitNonBlock() = withContext(Dispatchers.Default) {
        zoomToFit()
    }

    /**
     * Veraendert die interne Transformationsmatrix so, dass in das
     * Zentrum des Anzeigebereiches herein- bzw. herausgezoomt wird
     *
     * @param _factor Der Faktor um den herein- bzw. herausgezoomt wird
     */
    fun zoom(_factor: Double) {}

    suspend fun zoomNonBlock(_factor: Double) = withContext(Dispatchers.Default) {
        zoom(_factor)
    }

    /**
     * Veraendert die interne Transformationsmatrix so, dass an dem
     * uebergebenen Punkt herein- bzw. herausgezoomt wird
     *
     * @param _pt Der Punkt an dem herein- bzw. herausgezoomt wird
     * @param _factor Der Faktor um den herein- bzw. herausgezoomt wird
     */
    fun zoom(_pt: Point, _factor: Double) {}

    suspend fun zoomNonBlock(_pt: Point, _factor: Double) = withContext(Dispatchers.Default) {
        zoom(_pt, _factor)
    }

    /**
     * Ermittelt die gemeinsame BoundingBox der uebergebenen Polygone
     *
     * @param _poly Die Polygone, fuer die die BoundingBox berechnet
     * werden soll
     * @return Die BoundingBox
     */
    fun getMapBounds(_poly: Vector<Polygon>): Rectangle {
        return Rectangle()
    }

    suspend fun getMapBoundsNonBlocking(_poly: Vector<Polygon>): Rectangle = withContext(Dispatchers.Default) {
        getMapBounds(_poly)
    }

    /**
     * Veraendert die interne Transformationsmatrix so, dass
     * die zu zeichnenden Objekt horizontal verschoben werden.
     *
     * @param _delta Die Strecke, um die horizontal verschoben werden soll
     */
    fun scrollHorizontal(_delta: Int) {}

    suspend fun scrollHorizontalNonBlocking(_delta: Int) = withContext(Dispatchers.Default) {
        scrollHorizontal(_delta)
    }

    /**
     * Veraendert die interne Transformationsmatrix so, dass
     * die zu zeichnenden Objekt vertikal verschoben werden.
     *
     * @param _delta Die Strecke, um die vertikal verschoben werden soll
     */
    fun scrollVertical(_delta: Int) {}

    suspend fun scrollVerticalNonBlocking(_delta: Int) = withContext(Dispatchers.Default) {
        scrollVertical(_delta)
    }
}