package at.shehata.ex4.interfaces

import at.shehata.ex4.nmea.NMEAInfo

/**
 * Interface for the new info from
 * the parser
 */
interface PositionUpdateListener {
    /**
     * Notifies the listeners with the infos
     * received from a message block
     *
     * @param _info The Object with all the relevant information
     */
	fun update(_info: NMEAInfo)
}