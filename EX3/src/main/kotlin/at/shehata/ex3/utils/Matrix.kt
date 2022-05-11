package at.shehata.ex3.utils

import at.shehata.ex3.gis.extensions.plus
import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.geom.Point2D
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * The 2d size of the Matrix
 */
private const val MATRIX_SIZE = 9

/**
 * Matrix Rows, because we want a 3x3 Matrix
 */
private const val MATRIX_ROWS = 3

/**
 * Matrix Columns, because we want a 3x3 Matrix
 */
private const val MATRIX_COLUMNS = 3

/**
 * Offset for the first row because we are using a 1d
 * Array instead of a 2d
 */
private const val FIRST_ROW_INDEX = 0

/**
 * Offset for the second row because we are using a 1d
 * Array instead of a 2d
 */
private const val SECOND_ROW_INDEX = 3

/**
 * Offset for the third row because we are using a 1d
 * Array instead of a 2d
 */
private const val THIRD_ROW_INDEX = 6

/**
 * test functions of the Matrix class
 */
fun testZTF() {
    val world = Rectangle(47944531, 608091485, 234500, 213463)
    val win = Rectangle(0, 0, 640, 480)
    val transformed = Matrix.zoomToFit(world, win)

    val projected = transformed * world
    val resultProjected = Rectangle(56, 0, 527, 480)
    println(resultProjected + " - " + projected.contains(resultProjected))

    val invProjected = transformed.inverse() * projected
    val resultInv = Rectangle(47944376, 608091929, 234364, 213018)
    println(invProjected + " - " + invProjected.contains(resultInv))

    val pointZoomed = Matrix.zoomPoint(transformed, Point(70, 20), 1.1)
    println(pointZoomed)
}

/**
 * The Class is there to help with Matrix calculation
 * like multiplication and to provide transformation matrices
 *
 * @param mMatrix the Array to init the matrix
 */
data class Matrix(private val mMatrix: DoubleArray) {
    init {
        when {
            mMatrix.size < MATRIX_SIZE -> throw Exception("Matrix too small")
            mMatrix.size > MATRIX_SIZE -> throw Exception("Matrix too big")
        }
    }

    /**
     * Default constructor of no array is provided
     */
    constructor() : this(DoubleArray(9) { 0.0 })

    /**
     * Our static elements
     */
    companion object {
        /**
         * Creates a translation Matrix for the provided point
         *
         * @param Point a point that includes the translation values
         * @return The translation matrix
         * @see java.awt.Point
         */
        fun translate(_pt: Point): Matrix = translate(_pt.x.toDouble(), _pt.y.toDouble())

        /**
         * Liefert eine Translationsmatrix
         *
         * @param _x Der Translationswert der Matrix in X-Richtung
         * @param _y Der Translationswert der Matrix in Y-Richtung
         * @return Die Translationsmatrix
         */
        fun translate(_x: Double, _y: Double): Matrix = Matrix(
            doubleArrayOf(
                1.0, 0.0, _x,
                0.0, 1.0, _y,
                0.0, 0.0, 1.0
            )
        )

        /**
         * Liefert eine Skalierungsmatrix
         *
         * @param _scaleVal Der Skalierungswert der Matrix
         * @return Die Skalierungsmatrix
         */
        fun scale(_scaleVal: Double): Matrix = Matrix(
            doubleArrayOf(
                _scaleVal, 0.0, 0.0,
                0.0, _scaleVal, 0.0,
                0.0, 0.0, 1.0
            )
        )

        /**
         * Liefert eine Rotationsmatrix
         *
         * @param _alpha Der Winkel (in rad), um den rotiert werden
         * soll
         * @return Die Rotationsmatrix
         */
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

        /**
         * Liefert eine Spiegelungsmatrix (X-Achse)
         *
         * @return Die Spiegelungsmatrix
         */
        fun mirrorX(): Matrix = Matrix(
            doubleArrayOf(
                1.0, 0.0, 0.0,
                0.0, -1.0, 0.0,
                0.0, 0.0, 1.0
            )
        )

        /**
         * Liefert eine Spiegelungsmatrix (Y-Achse)
         *
         * @return Die Spiegelungsmatrix
         */
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
            _win.bounds.width.toDouble() / _world.bounds.width.toDouble()

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
            _win.bounds.height.toDouble() / _world.bounds.height.toDouble()

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
            val originMatrix = translate(-_world.centerX, -_world.centerY)
            val scaleMatrix = scale(min(getZoomFactorX(_world, _win), getZoomFactorY(_world, _win)))
            val mirrorMatrix = mirrorX()
            val centreMatrix = translate(_win.centerX, _win.centerY)

            return centreMatrix * mirrorMatrix * scaleMatrix * originMatrix
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
        fun zoomPoint(_old: Matrix, _zoomPt: Point, _zoomScale: Double): Matrix {
            val originMatrix = translate(-_zoomPt.x.toDouble(), -_zoomPt.y.toDouble())
            val scaleMatrix = scale(_zoomScale)
            val centreMatrix = translate(_zoomPt.x.toDouble(), _zoomPt.y.toDouble())

            return centreMatrix * scaleMatrix * originMatrix * _old
        }
    }

    /**
     * Overloads the '*' to allow multiplication without calling the multiply function
     */
    operator fun times(_other: Point): Point = multiply(_other)
    operator fun times(_other: Matrix): Matrix = multiply(_other)
    operator fun times(_other: Polygon): Polygon = multiply(_other)
    operator fun times(_other: Rectangle): Rectangle = multiply(_other)
    operator fun times(_other: Point2D.Double): Point2D.Double = multiply(_other)

    /**
     * Liefert die Invers-Matrix der Transformationsmatrix
     * @return Die Invers-Matrix
     */
    fun inverse(): Matrix {
        val determinant = (0 until MATRIX_ROWS).foldIndexed(0.0) { i, acc, _ ->
            val determinantOf2x2 = mMatrix[SECOND_ROW_INDEX + ((i + 1) % MATRIX_COLUMNS)] *
                    mMatrix[THIRD_ROW_INDEX + ((i + 2) % MATRIX_COLUMNS)] -
                    mMatrix[SECOND_ROW_INDEX + ((i + 2) % MATRIX_COLUMNS)] *
                    mMatrix[THIRD_ROW_INDEX + ((i + 1) % MATRIX_COLUMNS)]

            acc + mMatrix[FIRST_ROW_INDEX + i] * determinantOf2x2
        }

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

    /**
     * Multipliziert einen Punkt mit der Matrix und liefert das
     * Ergebnis der Multiplikation zurück
     *
     * @param _pt Der Punkt, der mit der Matrix multipliziert
     * werden soll
     * @return Ein neuer Punkt, der das Ergebnis der
     * Multiplikation repräsentiert
     * @see java.awt.Point
     */
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

    fun multiply(_pt: Point2D.Double): Point2D.Double {
        val destX = mMatrix[0 * MATRIX_COLUMNS + 0] * _pt.x + mMatrix[0 * MATRIX_COLUMNS + 1] * _pt.y
        val destY = mMatrix[1 * MATRIX_COLUMNS + 0] * _pt.x + mMatrix[1 * MATRIX_COLUMNS + 1] * _pt.y
        return Point2D.Double(destX, destY)
    }

    /**
     * Liefert eine Matrix, die das Ergebnis einer Matrizen-
     * multiplikation zwischen dieser und der übergebenen Matrix
     * ist
     *
     * @param _other Die Matrix mit der Multipliziert werden soll
     * @return Die Ergebnismatrix der Multiplikation
     */
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

    /**
     * Multipliziert ein Polygon mit der Matrix und liefert das
     * Ergebnis der Multiplikation zurück
     *
     * @param _poly Das Polygon, das mit der Matrix multipliziert
     * werden soll
     * @return Ein neues Polygon, das das Ergebnis der
     * Multiplikation repräsentiert
     * @see java.awt.Poylygon
     */
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

    /**
     * Multipliziert ein Rechteck mit der Matrix und liefert das
     * Ergebnis der Multiplikation zurück
     *
     * @param _rect Das Rechteck, das mit der Matrix multipliziert
     * werden soll
     * @return Ein neues Rechteck, das das Ergebnis der
     * Multiplikation repräsentiert
     * @see java.awt.Rectangle
     */
    fun multiply(_rect: Rectangle): Rectangle {
        val upperCorner = this * Point(_rect.x, _rect.y)
        val lowerCorner = this * Point(_rect.x + _rect.width, _rect.y + _rect.height)

        return Rectangle(upperCorner.x, upperCorner.y, 0, 0).apply {
            add(lowerCorner)
        }
    }

    /**
     * Liefert eine String-Repräsentation der Matrix
     *
     * @return Ein String mit dem Inhalt der Matrix
     * @see kotlin.String
     */
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