package at.shehata.ex1.gis

import at.shehata.ex1.interfaces.IDataObserver
import java.awt.Color
import java.awt.Polygon
import java.awt.Image
import java.awt.image.BufferedImage

open class GISModel {
    private var mWidth: Int = 1
    private var mHeight: Int = 1

    private lateinit var mImage: Image
    private lateinit var mObserver: IDataObserver

    private val mData = mutableListOf<Polygon>()

    fun generateRndHome() {
        val deltaX = (0..mWidth - 40).random()
        val deltaY = (0..mHeight - 50).random()
        val x = intArrayOf(deltaX + 10, deltaX + 40, deltaX + 40, deltaX + 25, deltaX + 10, deltaX + 10)
        val y = intArrayOf(deltaY + 10, deltaY + 10, deltaY + 30, deltaY + 50, deltaY + 30, deltaY + 10)
        mData.add(Polygon(x, y, x.size))
        repaint()
    }

    fun generateHome(_x: Int, _y: Int) {
        val startX = _x - 10
        val startY = _y - 40
        val x = intArrayOf(startX + 10, startX + 40, startX + 40, startX + 25, startX + 10, startX + 10)
        val y = intArrayOf(startY + 10, startY + 10, startY + 30, startY + 50, startY + 30, startY + 10)
        mData.add(Polygon(x, y, x.size))
        repaint()
    }

    fun initCanvas(): Image = BufferedImage(mWidth, mHeight, BufferedImage.TYPE_INT_RGB)

    fun repaint() {
        mImage.graphics.color = Color.BLUE
        mData.forEach { mImage.graphics.fillPolygon(it) }
        update(mImage)
    }

    fun setWidth(_width: Int) {
        mWidth = _width
        mImage = initCanvas()
        repaint()
    }

    fun setHeight(_height: Int) {
        mHeight = _height
        mImage = initCanvas()
        repaint()
    }

    protected fun update(_house: Image) {
        mObserver.update(_house)
    }

    fun addMapObserver(_observer: IDataObserver) {
        mObserver = _observer
    }
}