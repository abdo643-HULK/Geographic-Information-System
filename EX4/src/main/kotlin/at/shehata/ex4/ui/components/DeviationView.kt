package at.shehata.ex4.ui.components

import at.shehata.ex4.interfaces.PositionUpdateListener
import at.shehata.ex4.nmea.NMEAInfo
import at.shehata.ex4.utils.Matrix
import at.shehata.ex4.utils.drawCircle
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.Background
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import java.awt.Rectangle
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import kotlin.math.abs
import kotlin.math.sqrt

class DeviationView : Pane(), PositionUpdateListener {
    companion object {
        private const val POINT_SIZE = 8.0
        private const val POINT_SIZE_HALF = POINT_SIZE / 2
    }

    private var mWorldMatrix = Matrix()

    private val mPostions = mutableListOf<Point2D.Float>()
    private val mDiagonal
        get() = width - 50

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
//                println("$pt1, $pt2")

                lineWidth = 3.0
                fillOval(pt1.x - POINT_SIZE_HALF, pt1.y - POINT_SIZE_HALF, POINT_SIZE, POINT_SIZE)
                strokeLine(pt1.x, pt1.y, pt2.x, pt2.y)

                fill = Color.RED
                fillOval(pt2.x - POINT_SIZE_HALF, pt2.y - POINT_SIZE_HALF, POINT_SIZE, POINT_SIZE)
            }
        }
    }

    /**
     * Ermittelt die gemeinsame BoundingBox der übergebenen Polygone
     *
     * @return Die BoundingBox
     */
    private fun getMapBounds(): Rectangle {
        return when (mPostions.isEmpty()) {
            true -> Rectangle(0, 0, mCanvas.width.toInt(), mCanvas.height.toInt())
//            false -> Rectangle2D.Float(mPostions[0].x, mPostions[0].y, abs(mPostions[0].x), abs(mPostions[0].y))
//                .apply {
//                    mPostions
//                        .iterator()
//                        .apply { next() }
////                        .forEach { add(it) }
//                        .forEach { add(Rectangle2D.Float(it.x, it.y, abs(it.x), abs(it.y))) }
//                }.bounds
            false -> Path2D.Double()
                .apply {
                    mPostions
                        .iterator()
                        .apply {
                            val first = next()
                            moveTo(first.getX(), first.getX())
                        }
                        .forEach { lineTo(it.getX(), it.getY()) }
                    closePath()
                }.bounds
        }
    }

    private fun updateWorldMatrix() {
        val squareWidth = sqrt(2.0) * (mDiagonal / 2)
        val canvasCenterLeft = width / 2 - squareWidth / 2
        val canvasCenterTop = height / 2 - squareWidth / 2
        val rect = Rectangle2D.Double(canvasCenterLeft, canvasCenterTop, squareWidth, squareWidth)

        val bounds = getMapBounds()
        mWorldMatrix = Matrix.zoomToFit(bounds, rect.bounds)
        println("$bounds, ${rect.bounds} , $mWorldMatrix")
    }

    override fun update(_info: NMEAInfo) {
        if (_info.mLatitude == null || _info.mLongitude == null) return
        mPostions.add(Point2D.Float(_info.mLongitude!!, _info.mLatitude!!))
        updateWorldMatrix()
        draw()
    }
}
