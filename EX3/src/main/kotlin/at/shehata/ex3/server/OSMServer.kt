package at.shehata.ex3.server

import at.shehata.ex3.client.gis.drawingcontexts.OSMDrawingContext
import at.shehata.ex3.utils.GeoObject
import org.postgis.Geometry
import org.postgis.PGgeometry
import org.postgresql.PGConnection
import org.postgresql.util.PGobject
import java.awt.Polygon
import java.awt.Rectangle
import java.sql.Connection
import java.sql.DriverManager

private enum class OSM_Landuse(value: Int) {
    residential(5001),
    industrial(5002),
    commercial(5003),
    forest(5004),
    grass(5005),
    meadow(5006)
}

private enum class OSM_Natural(value: Int) {
    grassland(6001),
    wood(6002),
    water(6005)
}

private const val NATURAL_QUERY =
    "SELECT * FROM osm_natural " +
            "WHERE osm_natural.type " +
            "IN (6001, 6002, 6005)"

private const val LANDUSE_QUERY =
    "SELECT * FROM osm_landuse " +
            "WHERE osm_landuse.type " +
            "IN (5001, 5002, 5003, 5004, 5005, 5006)"

private const val BUILDING_QUERY = "SELECT * FROM osm_building"

class OSMServer {
    private val mContext by lazy { OSMDrawingContext() }

    @get:JvmName("getDrawingContext")
    val drawingContext
        get() = mContext

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

    fun loadData(): List<List<GeoObject>> {
        val data = mutableListOf<List<GeoObject>>()
        try {
            createConnection("osm-hagenberg").apply {
                /* Create a statement and execute a select query. */
                val stmt = (this as Connection).createStatement()

                data += arrayOf(NATURAL_QUERY, LANDUSE_QUERY, BUILDING_QUERY).map {
                    val result = stmt.executeQuery(it)
                    val data = mutableListOf<GeoObject>()
                    while (result.next()) {
                        val id = result.getString("id")
                        val type = result.getInt("type")
                        val geom = result.getObject("geom") as PGgeometry
                        when (geom.geoType) {
                            Geometry.POLYGON -> {
                                val wkt = geom.toString()
                                val poly = org.postgis.Polygon(wkt)
                                if (poly.numRings() < 1) continue
                                data += GeoObject(id, type, convertPolygon(poly))
                            }
                            Geometry.MULTIPOLYGON -> {
                                val wkt = geom.toString()
                                val multiPoly = org.postgis.MultiPolygon(wkt)
                                multiPoly.polygons.forEach {
                                    if (it.numRings() < 1) return@forEach
                                    data += GeoObject(id, type, convertPolygon(it))
                                }
                            }
                            Geometry.POINT -> {}
                            Geometry.MULTIPOINT -> {}
                            Geometry.LINESTRING -> {}
                            Geometry.MULTILINESTRING -> {}
                        }
                    }
                    data
                }
                stmt.close()
                close()
            }
        } catch (_e: Exception) {
            _e.printStackTrace()
        }

        return data
    }

//                    prepareStatement(
//                        "SELECT * FROM osm_landuse " +
//                                "WHERE osm_landuse.geom " +
//                                "&& ST_MakeEnvelope(?,?,?,?) " +
//                                "AND osm_landuse.type IN (5001, 5002, 5003, 5004, 5005, 5006)"
//                    )

    fun getArea(_boundingBox: Rectangle): List<GeoObject> {
        val data = mutableListOf<GeoObject>()
        try {
            createConnection("osm-hagenberg").apply {
                /* Create a statement and execute a select query. */
                val stmt = (this as Connection).createStatement()
                val envelope = _boundingBox.let { "ST_MakeEnvelope(${it.minX},${it.minY},${it.maxX},${it.maxY})" }
                val first = "SELECT * FROM osm_natural " +
                        "WHERE osm_natural.geom " +
                        "&& $envelope " +
                        "AND osm_natural.type IN (6001, 6002, 6005)"

                val second = "SELECT * FROM osm_landuse " +
                        "WHERE osm_landuse.geom " +
                        "&& $envelope " +
                        "AND osm_landuse.type IN (5001, 5002, 5003, 5004, 5005, 5006)"

                val third = "SELECT * FROM osm_building " +
                        "WHERE osm_building.geom " +
                        "&& $envelope"

                data += arrayOf(first, second, third).map {
                    val result = stmt.executeQuery(it)
                    val data = mutableListOf<GeoObject>()
                    while (result.next()) {
                        val id = result.getString("id")
                        val type = result.getInt("type")
                        val geom = result.getObject("geom") as PGgeometry
                        when (geom.geoType) {
                            Geometry.POLYGON -> {
                                val wkt = geom.toString()
                                val poly = org.postgis.Polygon(wkt)
                                if (poly.numRings() < 1) continue
                                data += GeoObject(id, type, convertPolygon(poly))
                            }
                            Geometry.MULTIPOLYGON -> {
                                val wkt = geom.toString()
                                val multiPoly = org.postgis.MultiPolygon(wkt)
                                multiPoly.polygons.forEach {
                                    if (it.numRings() < 1) return@forEach
                                    data += GeoObject(id, type, convertPolygon(it))
                                }
                            }
                            Geometry.POINT -> {}
                            Geometry.MULTIPOINT -> {}
                            Geometry.LINESTRING -> {}
                            Geometry.MULTILINESTRING -> {}
                        }
                    }
                    data
                }.flatten()
                stmt.close()
                close()
            }
        } catch (_e: Exception) {
            _e.printStackTrace()
        }

        return data
    }

//    fun getDrawingContext() = mContext
}