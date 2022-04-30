package at.shehata.ex2.gis

import de.intergis.JavaClient.comm.CgConnection
import de.intergis.JavaClient.comm.CgGeoConnection
import de.intergis.JavaClient.comm.CgGeoInterface
import de.intergis.JavaClient.gui.IgcConnection
import java.awt.Polygon
import java.util.*

open class DummyGIS {
    companion object {
        @JvmStatic
        fun main(_argv: Array<String>) {
            val server = DummyGIS()
            if (server.init()) {
                val objects = server.extractData("select * from data where type = 1101")
            }
        }
    }

    // die Verbindung zum Geo-Server
    private val mGeoconnection by lazy {
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

    // das Anfrage-Interface des Geo-Servers
    private val mGeointerface by lazy {
        // das Anfrage-Interface des Servers wird abgeholt
        mGeoconnection.getInterface()
    }

    fun init(): Boolean {
        try {
            mGeoconnection
            mGeointerface
            return true
        } catch (_e: Exception) {
            _e.printStackTrace()
        }
        return false
    }

    /**
     * Extrahiert einige Geoobjekte aus dem Server
     */
    fun extractData(_stmt: String): Vector<Polygon>? {
        try {
            val stmt = mGeointerface.Execute(_stmt)
            val cursor = stmt.cursor
            val objectContainer = Vector<Polygon>()
            while (cursor.next()) {
                val obj = cursor.getObject()
                println("NAME --> " + obj.name)
                println("TYP  --> " + obj.category)

                val parts = obj.parts
                for (i in parts.indices) {
                    println("PART $i")
                    val pointCount = parts[i].pointCount
                    val xArray = parts[i].x
                    val yArray = parts[i].y
                    val poly = Polygon(xArray, yArray, pointCount)
                    for (j in 0 until pointCount) {
                        println("[" + xArray[j] + " ; " + yArray[j] + "]")
                    } // for j
                    objectContainer.addElement(poly)
                } // for i
                println()
            } // while cursor
            return objectContainer
        } catch (_e: Exception) {
            _e.printStackTrace()
        }
        return null
    }

}