package at.shehata.ex4.interfaces

import at.shehata.ex4.nmea.NMEAInfo
import java.awt.Image

interface PositionUpdateListener {
    fun update(_info: NMEAInfo)
}