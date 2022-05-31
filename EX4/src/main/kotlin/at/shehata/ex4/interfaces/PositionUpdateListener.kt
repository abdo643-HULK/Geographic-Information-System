package at.shehata.ex4.interfaces

import java.awt.Image

interface PositionUpdateListener {
    fun update(_img: Image, _scale: Int)
}