package at.shehata.ex1.gis

import at.shehata.ex1.interfaces.IDataObserver
import java.awt.Color
import java.awt.Polygon
import java.awt.Image
import java.awt.image.BufferedImage

/**
 * Contains the core logic that the controller calls
 */
class GISModel {
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
     * List of all Polygons to render
     */
    private val mData = mutableListOf<Polygon>()

    /**
     * Generates a house at a random position inside the canvas
     */
    fun generateRndHome() {
        val deltaX = (0..mWidth - 40).random()
        val deltaY = (0..mHeight - 50).random()
        val x = intArrayOf(deltaX + 10, deltaX + 40, deltaX + 40, deltaX + 25, deltaX + 10, deltaX + 10)
        val y = intArrayOf(deltaY + 10, deltaY + 10, deltaY + 30, deltaY + 50, deltaY + 30, deltaY + 10)
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
        val startY = _y - 40
        val x = intArrayOf(startX + 10, startX + 40, startX + 40, startX + 25, startX + 10, startX + 10)
        val y = intArrayOf(startY + 10, startY + 10, startY + 30, startY + 50, startY + 30, startY + 10)
        mData.add(Polygon(x, y, x.size))
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
}