package at.shehata.ex3.server

import at.shehata.ex3.client.gis.drawingcontexts.VerwaltungsgrenzenDrawingContext
import at.shehata.ex3.utils.GeoObject
import org.postgis.Geometry
import org.postgis.PGgeometry
import org.postgresql.PGConnection
import org.postgresql.util.PGobject
import java.awt.Polygon
import java.sql.Connection
import java.sql.DriverManager

class VerwaltungsgrenzenServer {
    private val mContext by lazy { VerwaltungsgrenzenDrawingContext() }

    init {
        /* Load the JDBC driver. */
        org.postgresql.Driver::javaClass
    }

    fun getDrawingContext() = mContext

    private fun createConnection(_db: String): PGConnection {
        val url = "jdbc:postgresql://localhost:5432/$_db"
        val conn = DriverManager.getConnection(url, "geo", "geo")
        return (conn as PGConnection).apply {
            addDataType("geometry", org.postgis.PGgeometry::class.java as Class<out PGobject?>)
            addDataType("box2d", org.postgis.PGbox2d::class.java as Class<out PGobject?>)
        }
    }

    private fun convertPolygon(_poly: org.postgis.Polygon): List<Polygon> {
        val list = mutableListOf<Polygon>()
        for (point in 0 until _poly.numRings()) {
            val ring = _poly.getRing(point)
            val xPoints = IntArray(ring.numPoints())
            val yPoints = IntArray(ring.numPoints())
            for (i in 0 until ring.numPoints()) {
                val pPG = ring.getPoint(i)
                xPoints[i] = pPG.x.toInt()
                yPoints[i] = pPG.y.toInt()
            }
            list += Polygon(xPoints, yPoints, ring.numPoints())
        }
        return list
    }

    fun load3857Data(): List<GeoObject> {
        val data = mutableListOf<GeoObject>()
        try {
            createConnection("osm").apply {
                /* Create a statement and execute a select query. */
                val stmt = (this as Connection).createStatement()
                val result = stmt.executeQuery("SELECT * FROM bundeslaender")

                while (result.next()) {
                    val id = result.getString("id")
                    val type = result.getInt("type")
                    val geom = result.getObject("geom") as PGgeometry
                    when (geom.geoType) {
                        Geometry.POLYGON -> {
                            val wkt = geom.toString()
                            val poly = org.postgis.Polygon(wkt)
                            if (poly.numRings() < 1) continue
//                            data += GeoObject(id, type, convertPolygon(poly))
                        }
                        Geometry.MULTIPOLYGON -> {
                            val wkt = geom.toString()
                            val multiPoly = org.postgis.MultiPolygon(wkt)
                            multiPoly.polygons.forEach {
                                if (it.numRings() < 1) return@forEach
//                                data += GeoObject(id, type, convertPolygon(it))
                            }
                        }
                        Geometry.POINT -> {}
                        Geometry.MULTIPOINT -> {}
                        Geometry.LINESTRING -> {}
                        Geometry.MULTILINESTRING -> {}
                    }
                }
                stmt.close()
                close()
            }
        } catch (_e: Exception) {
            _e.printStackTrace()
        }

        return data
    }

    fun load4326Data(): List<GeoObject> {
        val data = mutableListOf<GeoObject>()
        try {
            createConnection("osm-4326").apply {
                /* Create a statement and execute a select query. */
                val stmt = (this as Connection).createStatement()
                val result = stmt.executeQuery("SELECT * FROM bundeslaender")

                while (result.next()) {
                    val id = result.getString("id")
                    val type = result.getInt("type")
                    val geom = result.getObject("geom") as PGgeometry
                    when (geom.geoType) {
                        Geometry.POLYGON -> {
                            val wkt = geom.toString()
                            val p = org.postgis.Polygon(wkt)
                            if (p.numRings() >= 1) {
                                val list = mutableListOf<Polygon>()
                                for (point in 0 until p.numRings()) {
                                    val ring = p.getRing(point)
                                    val xPoints = IntArray(ring.numPoints())
                                    val yPoints = IntArray(ring.numPoints())
                                    for (i in 0 until ring.numPoints()) {
                                        val pPG = ring.getPoint(i)
                                        xPoints[i] = (pPG.x * 100).toInt()
                                        yPoints[i] = (pPG.y * 150).toInt()
                                    }
                                    list += Polygon(xPoints, yPoints, xPoints.size)
                                }
//                                data += GeoObject(id, type, list)
                            }
                        }
//                        else -> continue
                    }
                }
                stmt.close()
                close()
            }
        } catch (_e: Exception) {
            _e.printStackTrace()
        }

        return data
    }
}