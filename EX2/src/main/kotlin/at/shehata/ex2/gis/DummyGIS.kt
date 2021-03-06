package at.shehata.ex2.gis

import at.shehata.ex2.utils.GeoObject
import de.intergis.JavaClient.comm.CgConnection
import de.intergis.JavaClient.gui.IgcConnection
import java.awt.Polygon

/**
 * Client class that calls the Server for the data to draw
 */
open class DummyGIS {
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
            mGeoConnection
            mGeoInterface
            return true
        } catch (_e: Exception) {
            _e.printStackTrace()
        }
        return false
    }

//    fun extractData(_stmt: String): Vector<Polygon>? {
//        try {
//            val stmt = mGeointerface.Execute(_stmt)
//            val cursor = stmt.cursor
//            val objectContainer = Vector<Polygon>()
//            while (cursor.next()) {
//                val obj = cursor.getObject()
//                println("NAME --> " + obj.name)
//                println("TYP  --> " + obj.category)
//
//                val parts = obj.parts
//                for (i in parts.indices) {
//                    println("PART $i")
//                    val pointCount = parts[i].pointCount
//                    val xArray = parts[i].x
//                    val yArray = parts[i].y
//                    val poly = Polygon(xArray, yArray, pointCount)
//                    for (j in 0 until pointCount) {
//                        println("[" + xArray[j] + " ; " + yArray[j] + "]")
//                    } // for j
//                    objectContainer.addElement(poly)
//                } // for i
//                println()
//            } // while cursor
//            return objectContainer
//        } catch (_e: Exception) {
//            _e.printStackTrace()
//        }
//        return null
//    }

    /**
     * Extracts GeoObjects from the Server
     *
     * @return if everything works fine a list of the objects or null if something fails
     * @see at.shehata.ex2.utils.GeoObject
     */
    fun extractData(_stmt: String): List<GeoObject>? {
        try {
            val stmt = mGeoInterface.Execute(_stmt)
            val cursor = stmt.cursor
            val objectContainer = mutableListOf<GeoObject>()

            while (cursor.next()) {
                val obj = cursor.getObject()

                val parts = obj.parts
                for (i in parts.indices) {
                    val pointCount = parts[i].pointCount
                    val xArray = parts[i].x
                    val yArray = parts[i].y
                    val poly = Polygon(xArray, yArray, pointCount)
                    objectContainer.add(GeoObject(obj.name, obj.category, poly))
                } // for i
            } // while cursor

            return objectContainer
        } catch (_e: Exception) {
            _e.printStackTrace()
        }
        return null
    }

}