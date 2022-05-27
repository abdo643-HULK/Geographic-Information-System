package at.shehata.ex3.server

import at.shehata.ex3.client.gis.drawingcontexts.VerwaltungsgrenzenDrawingContext
import at.shehata.ex3.feature.geo.GeoObject
import at.shehata.ex3.feature.geo.objectpart.Area
import at.shehata.ex3.feature.geo.objectpart.GeoObjectPart
import at.shehata.ex3.feature.geo.objectpart.Line
import at.shehata.ex3.server.interfaces.Server
import org.intellij.lang.annotations.Language
import org.postgis.Geometry
import org.postgis.PGgeometry
import org.postgresql.PGConnection
import org.postgresql.util.PGobject
import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

class VerwaltungsgrenzenServer : Server {
	override val mDrawingContext by lazy { VerwaltungsgrenzenDrawingContext() }

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
				listOf(at.shehata.ex3.feature.geo.objectpart.Point(Point(pt.x.toInt(), pt.y.toInt())))
			}
			Geometry.LINESTRING -> {
				val line = org.postgis.LineString(wkt)
				listOf(Line(line.points.map { Point(it.x.toInt(), it.y.toInt()) }))
			}
			Geometry.POLYGON -> {
				val poly = org.postgis.Polygon(wkt)
				if (poly.numRings() < 1) return emptyList()
				val polys = convertPolygon(poly)
				val holes = polys.drop(1).map { Area(it) }
				listOf(Area(polys[0], holes))
			}
			Geometry.MULTIPOLYGON -> {
				val multiPoly = org.postgis.MultiPolygon(wkt)
				multiPoly.polygons.mapNotNull {
					if (it.numRings() < 1) return@mapNotNull null
					val converted = convertPolygon(it)
					val poly = converted[0]
					val holes = converted.drop(1).map { hole -> Area(hole) }
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
		return try {
			val conn = createConnection("osm")
			val stmt = (conn as Connection).createStatement()

			@Language("SQL")
			val query = "SELECT * FROM bundeslaender"

			val data = getObjects(stmt, query)

			stmt.close()
			conn.close()

			data
		} catch (_e: Exception) {
			_e.printStackTrace()
			emptyList()
		}
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
								val list = mutableListOf<Area>()
								for (point in 0 until p.numRings()) {
									val ring = p.getRing(point)
									val xPoints = IntArray(ring.numPoints())
									val yPoints = IntArray(ring.numPoints())
									for (i in 0 until ring.numPoints()) {
										val pPG = ring.getPoint(i)
										xPoints[i] = (pPG.x * 100).toInt()
										yPoints[i] = (pPG.y * 150).toInt()
									}
									list += Area(Polygon(xPoints, yPoints, xPoints.size), emptyList())
								}
								data += GeoObject(id, type, list)
							}
						}
						else -> continue
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

	override fun getArea(_boundingBox: Rectangle): List<GeoObject> {
		return try {
			val conn = createConnection("osm")
			val stmt = (conn as Connection).createStatement()
			val envelope = _boundingBox.let { "ST_MakeEnvelope(${it.minX},${it.minY},${it.maxX},${it.maxY})" }

			@Language("SQL")
			val query = "SELECT * FROM osm_building " +
					"WHERE osm_building.geom " +
					"&& $envelope"

			val obj = getObjects(stmt, query)

			stmt.close()
			conn.close()

			obj
		} catch (_e: Exception) {
			_e.printStackTrace()
			emptyList()
		}
	}
}