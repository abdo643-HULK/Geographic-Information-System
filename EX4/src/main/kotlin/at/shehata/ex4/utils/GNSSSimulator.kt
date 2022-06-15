package at.shehata.ex4.utils

import java.io.BufferedReader
import java.io.FileReader

/**
 * Class simulates a GNSS Receiver
 */
class GNSSSimulator(
    /**
     * Dateinamen der Datei, die die GNSS-NMEA-Informationen enthält
     */
    _fileName: String,
    /**
     * eine Angabe über die Verzögerungszeit, bis der nächste Block gesendet werden soll
     * (man kann die Verarbeitung mittels der Thread.sleep(_sleep)-Methode für n-Millisekunden „anhalten“)
     */
    private val mSleep: Int,
    /**
     * einen Filter-String, der den Anfang eines neuen Blocks signalisiert
     * (häufig wird als Erstes ein GGA-Datensatz übermittelt)
     */
    private val mFilter: String
) : BufferedReader(FileReader(_fileName)) {

    override fun readLine(): String? {
        return super.readLine()?.apply {
            if (contains(mFilter)) Thread.sleep(mSleep.toLong())
        }
    }
}