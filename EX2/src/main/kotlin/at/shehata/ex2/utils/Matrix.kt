package at.shehata.ex2.utils

import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle
import kotlin.math.cos
import kotlin.math.sin


internal const val MATRIX_SIZE = 9
internal const val MATRIX_ROWS = 3
internal const val MATRIX_COLUMNS = 3

internal const val FIRST_ROW_INDEX = 0
internal const val SECOND_ROW_INDEX = 3
internal const val THIRD_ROW_INDEX = 6

data class Matrix(private val mMatrix: DoubleArray) {
    init {
        when {
            mMatrix.size < MATRIX_SIZE -> throw Exception("Matrix too small")
            mMatrix.size > MATRIX_SIZE -> throw Exception("Matrix too big")
        }
    }

    companion object {
        fun translate(_pt: Point): Matrix = translate(_pt.x.toDouble(), _pt.y.toDouble())

        fun translate(_x: Double, _y: Double): Matrix = Matrix(
            doubleArrayOf(
                1.0, 0.0, _x,
                0.0, 1.0, _y,
                0.0, 0.0, 1.0
            )
        )

        fun scale(_scaleVal: Double): Matrix = Matrix(
            doubleArrayOf(
                _scaleVal, 0.0, 0.0,
                0.0, _scaleVal, 0.0,
                0.0, 0.0, 1.0
            )
        )

        fun rotate(_alpha: Double): Matrix {
            val alphaCos = cos(_alpha)
            val alphaSin = sin(_alpha)

            return Matrix(
                doubleArrayOf(
                    alphaCos, -alphaSin, 0.0,
                    alphaSin, alphaCos, 0.0,
                    0.0, 0.0, 1.0
                )
            )
        }

        fun mirrorX(): Matrix = Matrix(
            doubleArrayOf(
                1.0, 0.0, 0.0,
                0.0, -1.0, 0.0,
                0.0, 0.0, 1.0
            )
        )

        fun mirrorY(): Matrix = Matrix(
            doubleArrayOf(
                -1.0, 0.0, 0.0,
                0.0, 1.0, 0.0,
                0.0, 0.0, 1.0
            )
        )

        /**
         * Liefert den Faktor, der benötigt wird, um das _world-
         * Rechteck in das _win-Rechteck zu skalieren (einzupassen)
         * bezogen auf die X-Achse  Breite
         *
         * @param _world Das Rechteck in Weltkoordinaten
         * @param _win Das Rechteck in Bildschirmkoordinaten
         * @return Der Skalierungsfaktor
         * @see java.awt.Rectangle
         */
        fun getZoomFactorX(_world: Rectangle, _win: Rectangle) =
            _world.bounds.width.toDouble() / _win.bounds.width.toDouble()

        /**
         * Liefert den Faktor, der benötigt wird, um das _world-
         * Rechteck in das _win-Rechteck zu skalieren (einzupassen)
         * bezogen auf die Y-Achse  Höhe
         *
         * @param _world Das Rechteck in Weltkoordinaten
         * @param _win Das Rechteck in Bildschirmkoordinaten
         * @return Der Skalierungsfaktor
         * @see java.awt.Rectangle
         */
        fun getZoomFactorY(_world: Rectangle, _win: Rectangle) =
            _world.bounds.height.toDouble() / _win.bounds.height.toDouble()

        /**
         * Liefert eine Matrix, die alle notwendigen Transformationen
         * beinhaltet (Translation, Skalierung, Spiegelung und
         * Translation), um ein _world-Rechteck in ein _win-Rechteck
         * abzubilden
         *
         * @param _world Das Rechteck in Weltkoordinaten
         * @param _win Das Rechteck in Bildschirmkoordinaten
         * @return Die Transformationsmatrix
         * @see java.awt.Rectangle
         */
        fun zoomToFit(_world: Rectangle, _win: Rectangle): Matrix {
            // TODO
            val worldBB = _world.bounds
            val winBB = _win.bounds

            return Matrix(
                doubleArrayOf(
                    -1.0, 0.0, 0.0,
                    0.0, 1.0, 0.0,
                    0.0, 0.0, 1.0
                )
            )
        }

        /**
         * Liefert eine Matrix, die eine vorhandene Transformations-
         * matrix erweitert, um an einem bestimmten Punkt um einen
         * bestimmten Faktor in die Karte hinein- bzw. heraus zu
         * zoomen
         *
         * @param _old Die zu erweiternde Transformationsmatrix
         * @param _zoomPt Der Punkt an dem gezoomt werden soll
         * @param _zoomScale Der Zoom-Faktor um den gezoomt werden
         * soll
         * @return Die neue Transformationsmatrix
         * @see java.awt.Point
         */
        fun zoomPoint(_old: Matrix?, _zoomPt: Point?, _zoomScale: Double): Matrix {
            // TODO
            return Matrix(
                doubleArrayOf(
                    -1.0, 0.0, 0.0,
                    0.0, 1.0, 0.0,
                    0.0, 0.0, 1.0
                )
            )
        }
    }

    operator fun times(_other: Point): Point = multiply(_other)
    operator fun times(_other: Matrix): Matrix = multiply(_other)
    operator fun times(_other: Polygon): Polygon = multiply(_other)
    operator fun times(_other: Rectangle): Rectangle = multiply(_other)

    fun inverse(): Matrix {
        val determinant = (0 until MATRIX_ROWS).foldIndexed(0.0) { i, acc, _ ->
            val determinantOf2x2 = mMatrix[SECOND_ROW_INDEX + ((i + 1) % MATRIX_COLUMNS)] *
                    mMatrix[THIRD_ROW_INDEX + ((i + 2) % MATRIX_COLUMNS)] -
                    mMatrix[SECOND_ROW_INDEX + ((i + 2) % MATRIX_COLUMNS)] *
                    mMatrix[THIRD_ROW_INDEX + ((i + 1) % MATRIX_COLUMNS)]

            acc + mMatrix[FIRST_ROW_INDEX + i] * determinantOf2x2
        }
        println("determinant = $determinant")

        val inverseMatrix = DoubleArray(MATRIX_SIZE)
        for (i in 0 until MATRIX_ROWS) {
            for (j in 0 until MATRIX_COLUMNS) {
                val r0c0 = mMatrix[(MATRIX_ROWS * ((j + 1) % MATRIX_ROWS)) + ((i + 1) % MATRIX_COLUMNS)]
                val r0c1 = mMatrix[(MATRIX_ROWS * ((j + 2) % MATRIX_ROWS)) + ((i + 2) % MATRIX_COLUMNS)]
                val r1c0 = mMatrix[(MATRIX_ROWS * ((j + 1) % MATRIX_ROWS)) + ((i + 2) % MATRIX_COLUMNS)]
                val r1c1 = mMatrix[(MATRIX_ROWS * ((j + 2) % MATRIX_ROWS)) + ((i + 1) % MATRIX_COLUMNS)]
                val value = (r0c0 * r0c1) - (r1c0 * r1c1)

                inverseMatrix[MATRIX_ROWS * i + j] = value / determinant
            }
        }

        return Matrix(inverseMatrix)
    }

    fun multiply(_pt: Point): Point {
        val vec = doubleArrayOf(_pt.x.toDouble(), _pt.y.toDouble(), 1.0)

        val product = DoubleArray(2) { 0.0 }
        for (i in 0 until MATRIX_ROWS - 1) {
            for (j in 0 until MATRIX_COLUMNS) {
                product[i] += mMatrix[(i * MATRIX_COLUMNS) + j] * vec[j]
            }
        }

        return Point(product[0].toInt(), product[1].toInt())
    }

    fun multiply(_other: Matrix): Matrix {
        val product = DoubleArray(MATRIX_SIZE) { 0.0 }

        for (i in 0 until MATRIX_ROWS) {
            for (j in 0 until MATRIX_COLUMNS) { // columns from other matrix
                for (k in 0 until MATRIX_COLUMNS) {
                    product[(i * MATRIX_COLUMNS) + j] +=
                        mMatrix[(i * MATRIX_COLUMNS) + k] * _other.mMatrix[(k * MATRIX_COLUMNS) + j]
                }
            }
        }

        return Matrix(product)
    }

    fun multiply(_poly: Polygon): Polygon {
        val product = arrayOf(IntArray(_poly.xpoints.size), IntArray(_poly.ypoints.size))
        val xPoints = _poly.xpoints
        val yPoints = _poly.ypoints

        val tmpPoint = Point()
        for (i in 0 until _poly.npoints) {
            tmpPoint.move(xPoints[i], yPoints[i])
            val newPoint = this * tmpPoint
            product[0][i] = newPoint.x
            product[1][i] = newPoint.y
        }

        return Polygon(product[0], product[1], _poly.xpoints.size)
    }

    fun multiply(_rect: Rectangle): Rectangle {
        val upperCorner = this * Point(_rect.x, _rect.y)
        val lowerCorner = this * Point(_rect.x + _rect.width, _rect.y + _rect.height)

        return Rectangle(upperCorner.x, upperCorner.y, 0, 0).apply {
            add(lowerCorner)
        }
    }

    override fun toString(): String {
        return "Matrix([\n  ${mMatrix.asList().chunked(3).joinToString("\n  ")}\n])"
    }

    /**
     * is a kotlin provided method, where named parameters
     * can be used and underscore added to the parameter can throw
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Matrix

        if (!mMatrix.contentEquals(other.mMatrix)) return false

        return true
    }

    override fun hashCode(): Int {
        return mMatrix.contentHashCode()
    }
}