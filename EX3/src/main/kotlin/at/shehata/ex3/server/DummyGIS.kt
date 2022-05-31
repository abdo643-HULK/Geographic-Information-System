package at.shehata.ex3.server

import at.shehata.ex3.feature.drawingcontexts.DummyDrawingContext
import at.shehata.ex3.feature.geo.objectpart.Area
import at.shehata.ex3.feature.geo.GeoObject
import at.shehata.ex3.feature.SerializableGeoObject
import at.shehata.ex3.server.interfaces.Server
import de.intergis.JavaClient.comm.CgConnection
import de.intergis.JavaClient.comm.CgError
import de.intergis.JavaClient.gui.IgcConnection
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.awt.Polygon
import java.awt.Rectangle
import java.io.File
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.concurrent.schedule

/**
 * Statements used to test with the DummyGIS
 */
private val ROUTES = mapOf(
	"select * from data where type = 1101" to "./1.json",
	"select * from data where type in (233, 933, 934, 1101)" to "./2.json",
	"select * from data where id =\"ABCDEFG\" and type = 233" to "./3.json",
	"select * from data where type in (233, 931, 932, 933, 934, 1101)" to "./4.json",
	"select toPic(" +
			"10000, " +
			"rect (x,y,width,height), " +
			"\"ALK-Amtlich\", " +
			"\"Rasterizer\", " +
			"\"(" +
			"select * from data where intersects (" +
			"location, rect (x,y,width,height) and type in prestemp_types (\"ALK-Amtlich\")" +
			"), {}" +
			") " +
			"from dual" to null
)

/**
 * Client class that calls the Server for the data to draw
 */
open class DummyGIS : Server {
	override val mDrawingContext by lazy { DummyDrawingContext() }

	/**
	 * Create a connection to the Geo-Server,
	 * when it's called the first time.
	 */
	private val mGeoConnection by lazy {
		// der Geo-Server wird initialisiert
		IgcConnection(
			CgConnection(
				"admin",
				"admin",
				"T:localhost:4949",
//				"T:10.29.17.141:4949",
				null
			)
		)
	}

	/**
	 * Calls the Server for the Interfaces and
	 * saves them
	 */
	private val mGeoInterface by lazy { mGeoConnection.getInterface() }

	/**
	 * Initializes the Connection to the Server
	 *
	 * @return if the connection was successful or not
	 */
	fun init(): Boolean {
		try {
			mGeoInterface
			Timer("killConnection", false).schedule(500) {
				throw TimeoutException()
			}
			return true
		} catch (_e: Exception) {
			when (_e) {
				is TimeoutException -> {
					return true
				}
				is CgError -> when (_e.localizedMessage.endsWith("Connection refused")) {
					true -> return true
					false -> _e.printStackTrace()
				}
			}
		}
		return false
	}

	override fun loadData() = when (init()) {
		true -> extractData("SELECT * FROM data WHERE type in (233, 931, 932, 933, 934, 1101)")
			?.map { GeoObject(it.mId, it.mType, listOf(Area(it.mPoly, emptyList()))) } ?: emptyList()
		false -> emptyList()
	}

	override fun getArea(_boundingBox: Rectangle) = loadData()

	/**
	 * Extracts GeoObjects from the Server
	 *
	 * @return if everything works fine a list of the objects or null if something fails
	 * @see at.shehata.ex3.feature.SerializableGeoObject
	 */
	@OptIn(ExperimentalSerializationApi::class)
	fun extractData(_stmt: String): List<SerializableGeoObject>? {
		try {
			ROUTES[_stmt.lowercase()]?.let {
				return Json.decodeFromStream<Array<SerializableGeoObject>>(File(it).inputStream()).asList()
			}

			val stmt = mGeoInterface.Execute(_stmt)
			val cursor = stmt.cursor
			val objectContainer = mutableListOf<SerializableGeoObject>()

			while (cursor.next()) {
				val obj = cursor.getObject()

				val parts = obj.parts
				for (i in parts.indices) {
					val pointCount = parts[i].pointCount
					val xArray = parts[i].x
					val yArray = parts[i].y
					val poly = Polygon(xArray, yArray, pointCount)
					objectContainer += SerializableGeoObject(obj.name, obj.category, poly)
				} // for i
			} // while cursor

			return objectContainer
		} catch (_e: Exception) {
			_e.printStackTrace()
		}
		return null
	}


	/**
	 * Helper class to have offline support
	 * for the data. It writes the Data into a
	 * json file
	 */
	@OptIn(ExperimentalSerializationApi::class)
	fun loadDataToJson() {
		ROUTES.entries.forEach {
			try {
				val stmt = mGeoInterface.Execute(it.key)
				val cursor = stmt.cursor
				val objectContainer = mutableListOf<SerializableGeoObject>()

				while (cursor.next()) {
					val obj = cursor.getObject()

					val parts = obj.parts
					for (i in parts.indices) {
						val pointCount = parts[i].pointCount
						val xArray = parts[i].x
						val yArray = parts[i].y
						val poly = Polygon(xArray, yArray, pointCount)
						objectContainer.add(SerializableGeoObject(obj.name, obj.category, poly))
					} // for i
				} // while cursor
				it.value?.apply { Json.encodeToStream(objectContainer, File(this).outputStream()) }
			} catch (_e: Exception) {
				_e.printStackTrace()
			}
		}
	}
}