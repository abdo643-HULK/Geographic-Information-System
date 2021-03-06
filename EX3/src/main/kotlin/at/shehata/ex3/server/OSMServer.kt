package at.shehata.ex3.server

import at.shehata.ex3.feature.drawingcontexts.OSMDrawingContext
import at.shehata.ex3.feature.geo.GeoObject
import at.shehata.ex3.feature.geo.objectpart.Area
import at.shehata.ex3.feature.geo.objectpart.GeoObjectPart
import at.shehata.ex3.feature.geo.objectpart.Line
import at.shehata.ex3.feature.geo.objectpart.Point
import at.shehata.ex3.server.interfaces.Server
import org.intellij.lang.annotations.Language
import org.postgis.Geometry
import org.postgis.PGgeometry
import org.postgresql.PGConnection
import org.postgresql.util.PGobject
import java.awt.Polygon
import java.awt.Rectangle
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.awt.Point as AwtPoint

@Language("SQL")
private const val NATURAL_QUERY =
	"SELECT * FROM osm_natural " +
			"WHERE osm_natural.type " +
			"IN (6001, 6002, 6005)"

@Language("SQL")
private const val LANDUSE_QUERY =
	"SELECT * FROM osm_landuse " +
			"WHERE osm_landuse.type " +
			"IN (5001, 5002, 5003, 5004, 5005, 5006)"

@Language("SQL")
private const val HIGHWAY_QUERRY = "SELECT * FROM osm_highway " +
		"WHERE osm_highway.type " +
		"IN (1010, 1030)"

@Language("SQL")
private const val BUILDING_QUERY = "SELECT * FROM osm_building"

class OSMServer : Server {
	override val mDrawingContext by lazy { OSMDrawingContext() }

	/**
	 * Connects to the DB by logging in and
	 * loads the datatypes needed for PostGIS
	 *
	 * @param _db the DB to connect to
	 * @return the connection to the db
	 */
	private fun createConnection(_db: String): PGConnection {
		val url = "jdbc:postgresql://localhost:5432/$_db"
		val conn = DriverManager.getConnection(url, "geo", "geo")
		return (conn as PGConnection).apply {
			addDataType("geometry", org.postgis.PGgeometry::class.java as Class<out PGobject?>)
			addDataType("box2d", org.postgis.PGbox2d::class.java as Class<out PGobject?>)
		}
	}

	/**
	 * converts a PostGIS polygon to a List of AWT Polygons
	 *
	 * @param _poly the PostGIS polygon to convert
	 * @return a list of AWT Polygons
	 */
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

	/**
	 * Converts a geometry object to the corresponding GeoObjectPart type
	 *
	 * @param _geom the geometry object to convert
	 * @return a list of GeoObjectPart
	 */
	private fun extractObject(_geom: PGgeometry): List<GeoObjectPart> {
		val wkt = _geom.toString()
		return when (_geom.geoType) {
			Geometry.POINT -> {
				val pt = org.postgis.Point(wkt)
				listOf(Point(AwtPoint(pt.x.toInt(), pt.y.toInt())))
			}
			Geometry.LINESTRING -> {
				val line = org.postgis.LineString(wkt)
				listOf(Line(line.points.map { AwtPoint(it.x.toInt(), it.y.toInt()) }))
			}
			Geometry.POLYGON -> {
				val poly = org.postgis.Polygon(wkt)
				if (poly.numRings() < 1) return emptyList()
				val polys = convertPolygon(poly)
				val holes = polys
					.drop(1)
					.map { Area(it) }
				listOf(Area(polys[0], holes))
			}
			Geometry.MULTIPOLYGON -> {
				val multiPoly = org.postgis.MultiPolygon(wkt)
				multiPoly.polygons.mapNotNull {
					if (it.numRings() < 1) return@mapNotNull null
					val converted = convertPolygon(it)
					val poly = converted[0]
					val holes = converted
						.drop(1)
						.map { hole -> Area(hole) }
					Area(poly, holes)
				}
			}
			else -> emptyList()
		}
	}

	/**
	 * Executes the query and converts the results into a list
	 * of GeoObjects
	 *
	 * @param _stmt the statement to execute the query on
	 * @param _query the query to execute
	 * @return the list of created GeoObjects
	 */
	private fun getObjects(_stmt: Statement, _query: String): List<GeoObject> {
		val result = _stmt.executeQuery(_query)
		val objects = mutableListOf<GeoObject>()
		while (result.next()) {
			val id = result.getString("id")
			val type = result.getInt("type")
			val geom = result.getObject("geom") as PGgeometry
			objects += GeoObject(id, type, extractObject(geom))
		}

		return objects
	}

	override fun loadData(): List<GeoObject> {
		val data = mutableListOf<List<GeoObject>>()
		try {
			createConnection("osm-hagenberg").apply {
				/* Create a statement and execute a select query. */
				val stmt = (this as Connection).createStatement()

				@Language("SQL")
				val waterways = "SELECT * FROM osm_waterway"

				@Language("SQL")
				val places = "SELECT * FROM osm_place"
				data += arrayOf(
					NATURAL_QUERY,
					waterways,
					LANDUSE_QUERY,
					HIGHWAY_QUERRY,
					places,
					BUILDING_QUERY
				)
					.map { query -> getObjects(stmt, query) }

				stmt.close()
				close()
			}
		} catch (_e: Exception) {
			_e.printStackTrace()
		}

		return data.flatten()
	}

	override fun getArea(_boundingBox: Rectangle): List<GeoObject> {
		val data = mutableListOf<GeoObject>()
		try {
			createConnection("osm-hagenberg").apply {
				/* Create a statement and execute a select query. */
				val stmt = (this as Connection).createStatement()
				val envelope = _boundingBox.let { "ST_MakeEnvelope(${it.minX},${it.minY},${it.maxX},${it.maxY})" }

				@Language("SQL")
				val first = "SELECT * FROM osm_natural " +
						"WHERE osm_natural.geom " +
						"&& $envelope " +
						"AND osm_natural.type IN (6001, 6002, 6005)"

				@Language("SQL")
				val second = "SELECT * FROM osm_landuse " +
						"WHERE osm_landuse.geom " +
						"&& $envelope " +
						"AND osm_landuse.type IN (5001, 5002, 5003, 5004, 5005, 5006)"

				@Language("SQL")
				val third = "SELECT * FROM osm_highway " +
						"WHERE osm_highway.geom " +
						"&& $envelope " +
						"AND osm_highway.type IN (1010, 1030)"

				@Language("SQL")
				val fourth = "SELECT * FROM osm_building " +
						"WHERE osm_building.geom " +
						"&& $envelope"

				data += arrayOf(first, second, third, fourth)
					.map { query -> getObjects(stmt, query) }
					.flatten()

				stmt.close()
				close()
			}
		} catch (_e: Exception) {
			_e.printStackTrace()
		}

		return data
	}
}